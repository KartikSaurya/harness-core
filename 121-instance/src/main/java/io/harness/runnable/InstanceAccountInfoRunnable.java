/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.runnable;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.OwnedBy;
import io.harness.entities.DeploymentAccounts;
import io.harness.entities.DeploymentAccounts.DeploymentAccountsKeys;
import io.harness.entities.DeploymentSummary;
import io.harness.entities.DeploymentSummary.DeploymentSummaryKeys;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Singleton
@OwnedBy(DX)
@AllArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
public class InstanceAccountInfoRunnable implements Runnable {
  private MongoTemplate mongoTemplate;
  @Override
  public void run() {
    try {
      execute();
    } catch (Exception e) {
      log.error("Exception happened in InstanceAccount execute", e);
    }
  }

  public void execute() {
    Query query = new Query();
    final List<String> accountIdsToBeInserted = mongoTemplate.findDistinct(
        query, DeploymentSummaryKeys.accountIdentifier, DeploymentSummary.class, String.class);
    final List<String> accountIdsAlreadyExist = mongoTemplate.findDistinct(
        query, DeploymentAccountsKeys.accountIdentifier, DeploymentAccounts.class, String.class);
    accountIdsToBeInserted.removeAll(accountIdsAlreadyExist);
    if (accountIdsToBeInserted.size() != 0) {
      final List<DeploymentAccounts> deploymentAccountsDocuments =
          accountIdsToBeInserted.stream()
              .map(item
                  -> DeploymentAccounts.builder()
                         .accountIdentifier(item)
                         .instanceStatsMetricsPublisherIteration(0L)
                         .build())
              .collect(Collectors.toList());
      mongoTemplate.insertAll(deploymentAccountsDocuments);
    }
  }
}
