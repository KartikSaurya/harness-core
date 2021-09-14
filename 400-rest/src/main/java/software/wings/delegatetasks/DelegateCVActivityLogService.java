/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.delegatetasks;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import software.wings.service.intfc.verification.CVActivityLogService;
@TargetModule(HarnessModule._930_DELEGATE_TASKS)
public interface DelegateCVActivityLogService {
  Logger getLogger(String accountId, String cvConfigId, long dataCollectionMinute, String stateExecutionId,
      String prefix, long... prefixTimestampParams);
  default Logger getLogger(String accountId, String cvConfigId, long dataCollectionMinute, String stateExecutionId) {
    return getLogger(accountId, cvConfigId, dataCollectionMinute, stateExecutionId, "");
  }

  interface Logger extends CVActivityLogService.Logger {}
}
