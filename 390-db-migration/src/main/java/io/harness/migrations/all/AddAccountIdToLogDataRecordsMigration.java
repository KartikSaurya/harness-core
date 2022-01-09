/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.migrations.all;

import software.wings.service.impl.analysis.LogDataRecord.LogDataRecordKeys;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AddAccountIdToLogDataRecordsMigration extends AddAccountIdToCollectionUsingAppIdMigration {
  @Override
  protected String getCollectionName() {
    return "logDataRecords";
  }

  @Override
  protected String getFieldName() {
    return LogDataRecordKeys.accountId;
  }
}
