/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.migrations.all;

import static io.harness.persistence.HPersistence.DEFAULT_STORE;

import io.harness.migrations.Migration;

import software.wings.dl.WingsPersistence;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class DeleteCVCronMigration implements Migration {
  @Inject private WingsPersistence wingsPersistence;

  @Override
  public void migrate() {
    deleteCron("quartz_jobs", "ADMINISTRATIVE_CRON_GROUP");
    deleteCron("quartz_triggers", "ADMINISTRATIVE_CRON_GROUP");

    deleteCron("quartz_jobs", "EXECUTION_LOGS_PRUNE_CRON_GROUP");
    deleteCron("quartz_triggers", "EXECUTION_LOGS_PRUNE_CRON_GROUP");

    deleteCron("quartz_verification_jobs", "WORKFLOW_TIME_SERIES_VERIFY_CRON_GROUP");
    deleteCron("quartz_verification_triggers", "WORKFLOW_TIME_SERIES_VERIFY_CRON_GROUP");

    deleteCron("quartz_verification_jobs", "WORKFLOW_LOG_ANALYSIS_CRON_GROUP");
    deleteCron("quartz_verification_triggers", "WORKFLOW_LOG_ANALYSIS_CRON_GROUP");

    deleteCron("quartz_verification_jobs", "LOG_CLUSTER_CRON_GROUP");
    deleteCron("quartz_verification_triggers", "LOG_CLUSTER_CRON_GROUP");

    deleteCron("quartz_verification_jobs", "WORKFLOW_FEEDBACK_ANALYSIS_CRON_GROUP");
    deleteCron("quartz_verification_triggers", "WORKFLOW_FEEDBACK_ANALYSIS_CRON_GROUP");
  }

  private void deleteCron(String collectionName, String keyGroupName) {
    DBCollection collection = wingsPersistence.getCollection(DEFAULT_STORE, collectionName);
    collection.findAndRemove(new BasicDBObject("keyGroup", keyGroupName));
  }
}
