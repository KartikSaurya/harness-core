/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.repositories.executions;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.pms.plan.execution.beans.PipelineExecutionSummaryEntity;

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
@OwnedBy(PIPELINE)
public class PmsExecutionSummaryRepositoryCustomImpl implements PmsExecutionSummaryRepositoryCustom {
  private final MongoTemplate mongoTemplate;
  private final Duration RETRY_SLEEP_DURATION = Duration.ofSeconds(10);
  private final int MAX_ATTEMPTS = 3;

  @Override
  public PipelineExecutionSummaryEntity update(Query query, Update update) {
    RetryPolicy<Object> retryPolicy =
        getRetryPolicy("[Retrying]: Failed updating PipelineExecutionSummary; attempt: {}",
            "[Failed]: Failed updating PipelineExecutionSummary; attempt: {}");
    return Failsafe.with(retryPolicy)
        .get(()
                 -> mongoTemplate.findAndModify(
                     query, update, new FindAndModifyOptions().returnNew(true), PipelineExecutionSummaryEntity.class));
  }

  @Override
  public UpdateResult deleteAllExecutionsWhenPipelineDeleted(Query query, Update update) {
    RetryPolicy<Object> retryPolicy =
        getRetryPolicy("[Retrying]: Failed deleting PipelineExecutionSummary; attempt: {}",
            "[Failed]: Failed deleting PipelineExecutionSummary; attempt: {}");
    return Failsafe.with(retryPolicy)
        .get(() -> mongoTemplate.updateMulti(query, update, PipelineExecutionSummaryEntity.class));
  }

  @Override
  public Page<PipelineExecutionSummaryEntity> findAll(Criteria criteria, Pageable pageable) {
    try {
      Query query = new Query(criteria).with(pageable);
      List<PipelineExecutionSummaryEntity> projects = mongoTemplate.find(query, PipelineExecutionSummaryEntity.class);
      return PageableExecutionUtils.getPage(projects, pageable,
          () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), PipelineExecutionSummaryEntity.class));
    } catch (IllegalArgumentException ex) {
      log.error(ex.getMessage(), ex);
      throw new InvalidRequestException("Execution Status not found", ex);
    }
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
