/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.health;

import static io.harness.annotations.dev.HarnessTeam.CE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ccm.commons.entities.events.CeExceptionRecord;
import io.harness.ccm.commons.entities.events.CeExceptionRecord.CeExceptionRecordKeys;
import io.harness.persistence.HPersistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.query.Sort;

@Slf4j
@Singleton
@OwnedBy(CE)
public class CeExceptionRecordDao {
  @Inject private HPersistence persistence;

  public String save(io.harness.ccm.commons.entities.events.CeExceptionRecord exception) {
    return persistence.save(exception);
  }

  public io.harness.ccm.commons.entities.events.CeExceptionRecord getRecentException(
      String accountId, String clusterId, long recentTimestamp) {
    return persistence.createQuery(CeExceptionRecord.class)
        .field(CeExceptionRecordKeys.accountId)
        .equal(accountId)
        .field(CeExceptionRecordKeys.clusterId)
        .equal(clusterId)
        .field(CeExceptionRecordKeys.createdAt)
        .greaterThanOrEq(recentTimestamp)
        .order(Sort.descending(CeExceptionRecordKeys.createdAt))
        .get();
  }
}
