/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.repositories.environment.custom;

import io.harness.ng.core.environment.beans.Environment;
import io.harness.ng.core.environment.mappers.EnvironmentFilterHelper;

import com.google.inject.Inject;
import com.mongodb.client.result.UpdateResult;
import java.time.Duration;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.support.PageableExecutionUtils;

@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({ @Inject }))
@Slf4j
public class EnvironmentRepositoryCustomImpl implements EnvironmentRepositoryCustom {
  private final MongoTemplate mongoTemplate;
  private final Duration RETRY_SLEEP_DURATION = Duration.ofSeconds(10);
  private final int MAX_ATTEMPTS = 3;

  @Override
  public Page<Environment> findAll(Criteria criteria, Pageable pageable) {
    Query query = new Query(criteria).with(pageable);
    List<Environment> projects = mongoTemplate.find(query, Environment.class);
    return PageableExecutionUtils.getPage(
        projects, pageable, () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Environment.class));
  }

  @Override
  public Environment upsert(Criteria criteria, Environment environment) {
    Query query = new Query(criteria);
    Update updateOperations = EnvironmentFilterHelper.getUpdateOperations(environment);
    RetryPolicy<Object> retryPolicy = getRetryPolicy(
        "[Retrying]: Failed upserting Environment; attempt: {}", "[Failed]: Failed upserting Environment; attempt: {}");
    return Failsafe.with(retryPolicy)
        .get(()
                 -> mongoTemplate.findAndModify(query, updateOperations,
                     new FindAndModifyOptions().returnNew(true).upsert(true), Environment.class));
  }

  @Override
  public Environment update(Criteria criteria, Environment environment) {
    Query query = new Query(criteria);
    Update updateOperations = EnvironmentFilterHelper.getUpdateOperations(environment);
    RetryPolicy<Object> retryPolicy = getRetryPolicy(
        "[Retrying]: Failed updating Environment; attempt: {}", "[Failed]: Failed updating Environment; attempt: {}");
    return Failsafe.with(retryPolicy)
        .get(()
                 -> mongoTemplate.findAndModify(
                     query, updateOperations, new FindAndModifyOptions().returnNew(true), Environment.class));
  }

  public UpdateResult delete(Criteria criteria) {
    Query query = new Query(criteria);
    Update updateOperationsForDelete = EnvironmentFilterHelper.getUpdateOperationsForDelete();
    RetryPolicy<Object> retryPolicy = getRetryPolicy(
        "[Retrying]: Failed deleting Environment; attempt: {}", "[Failed]: Failed deleting Environment; attempt: {}");
    return Failsafe.with(retryPolicy)
        .get(() -> mongoTemplate.updateFirst(query, updateOperationsForDelete, Environment.class));
  }

  @Override
  public List<Environment> findAllRunTimeAccess(Criteria criteria) {
    Query query = new Query(criteria);
    return mongoTemplate.find(query, Environment.class);
  }

  private RetryPolicy<Object> getRetryPolicy(String failedAttemptMessage, String failureMessage) {
    return new RetryPolicy<>()
        .handle(OptimisticLockingFailureException.class)
        .handle(DuplicateKeyException.class)
        .withDelay(RETRY_SLEEP_DURATION)
        .withMaxAttempts(MAX_ATTEMPTS)
        .onFailedAttempt(event -> log.info(failedAttemptMessage, event.getAttemptCount(), event.getLastFailure()))
        .onFailure(event -> log.error(failureMessage, event.getAttemptCount(), event.getFailure()));
  }
}
