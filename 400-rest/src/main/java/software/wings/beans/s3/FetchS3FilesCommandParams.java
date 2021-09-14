/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans.s3;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.delegate.beans.executioncapability.ExecutionCapabilityDemander;
import io.harness.delegate.task.TaskParameters;
import io.harness.expression.ExpressionEvaluator;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.AwsConfig;
import software.wings.delegatetasks.delegatecapability.CapabilityHelper;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(HarnessTeam.CDP)
@TargetModule(HarnessModule._950_DELEGATE_TASKS_BEANS)
public class FetchS3FilesCommandParams implements TaskParameters, ExecutionCapabilityDemander {
  private List<S3FileRequest> s3FileRequests;
  private AwsConfig awsConfig;
  private List<EncryptedDataDetail> encryptionDetails;
  private String accountId;
  private String appId;
  private String activityId;
  private String executionLogName;

  @Override
  public List<ExecutionCapability> fetchRequiredExecutionCapabilities(ExpressionEvaluator maskingEvaluator) {
    return CapabilityHelper.generateDelegateCapabilities(awsConfig, getEncryptionDetails(), maskingEvaluator);
  }
}
