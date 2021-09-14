/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.cf;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.delegate.cf.PcfCommandTaskHandler;
import io.harness.delegate.task.pcf.CfCommandRequest;
import io.harness.delegate.task.pcf.response.CfCommandExecutionResponse;
import io.harness.exception.ExceptionUtils;
import io.harness.logging.CommandExecutionStatus;
import io.harness.security.encryption.EncryptedDataDetail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@OwnedBy(HarnessTeam.CDP)
public class PcfDelegateTaskHelper {
  @Inject private Map<String, PcfCommandTaskHandler> commandTaskTypeToTaskHandlerMap;

  public CfCommandExecutionResponse getPcfCommandExecutionResponse(CfCommandRequest cfCommandRequest,
      List<EncryptedDataDetail> encryptedDataDetails, boolean isInstanceSync,
      ILogStreamingTaskClient logStreamingTaskClient) {
    try {
      return commandTaskTypeToTaskHandlerMap.get(cfCommandRequest.getPcfCommandType().name())
          .executeTask(cfCommandRequest, encryptedDataDetails, isInstanceSync, logStreamingTaskClient);
    } catch (Exception ex) {
      log.error("Exception in processing PCF task [{}]", cfCommandRequest.toString(), ex);
      return CfCommandExecutionResponse.builder()
          .commandExecutionStatus(CommandExecutionStatus.FAILURE)
          .errorMessage(ExceptionUtils.getMessage(ex))
          .build();
    }
  }
}
