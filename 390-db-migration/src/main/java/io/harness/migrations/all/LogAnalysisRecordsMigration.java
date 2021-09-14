/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.migrations.all;

import static io.harness.data.structure.EmptyPredicate.isEmpty;

import static software.wings.beans.Base.ID_KEY2;

import io.harness.migrations.Migration;

import software.wings.dl.WingsPersistence;
import software.wings.service.impl.analysis.LogMLAnalysisRecord;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by rsingh on 3/26/18.
 */
@Slf4j
public class LogAnalysisRecordsMigration implements Migration {
  @Inject WingsPersistence wingsPersistence;

  @Override
  public void migrate() {
    DBCollection collection = wingsPersistence.getCollection(LogMLAnalysisRecord.class);
    BulkWriteOperation bulkWriteOperation = collection.initializeUnorderedBulkOperation();

    DBCursor logAnalysisRecords = collection.find();

    log.info("will go through " + logAnalysisRecords.size() + " records");

    int updated = 0;
    int batched = 0;
    while (logAnalysisRecords.hasNext()) {
      DBObject next = logAnalysisRecords.next();

      String uuId = (String) next.get("_id");
      String appId = (String) next.get("applicationId");
      if (isEmpty(appId)) {
        continue;
      }
      bulkWriteOperation
          .find(wingsPersistence.createQuery(LogMLAnalysisRecord.class).filter(ID_KEY2, uuId).getQueryObject())
          .updateOne(new BasicDBObject("$set", new BasicDBObject("appId", appId)));
      updated++;
      batched++;

      if (updated != 0 && updated % 1000 == 0) {
        bulkWriteOperation.execute();
        bulkWriteOperation = collection.initializeUnorderedBulkOperation();
        batched = 0;
        log.info("updated: " + updated);
      }
    }

    if (batched != 0) {
      bulkWriteOperation.execute();
      log.info("updated: " + updated);
    }

    log.info("Complete. Updated " + updated + " records.");
  }
}
