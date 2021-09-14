/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.helpers.ext.helm.request;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.task.helm.HelmCommandFlag;
import io.harness.k8s.model.HelmVersion;
import io.harness.logging.LogCallback;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.GitConfig;
import software.wings.beans.GitFileConfig;
import software.wings.service.impl.ContainerServiceParams;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by anubhaw on 4/2/18.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TargetModule(HarnessModule._950_DELEGATE_TASKS_BEANS)
@OwnedBy(CDP)
public class HelmReleaseHistoryCommandRequest extends HelmCommandRequest {
  public HelmReleaseHistoryCommandRequest(boolean mergeCapabilities) {
    super(HelmCommandType.RELEASE_HISTORY, mergeCapabilities);
  }

  @Builder
  public HelmReleaseHistoryCommandRequest(String accountId, String appId, String kubeConfigLocation, String commandName,
      String activityId, ContainerServiceParams containerServiceParams, String releaseName, GitConfig gitConfig,
      List<EncryptedDataDetail> encryptedDataDetails, LogCallback executionLogCallback, String commandFlags,
      HelmCommandFlag helmCommandFlag, HelmVersion helmVersion, String ocPath, String workingDir,
      List<String> variableOverridesYamlFiles, GitFileConfig gitFileConfig, boolean k8SteadyStateCheckEnabled,
      boolean mergeCapabilities, boolean isGitHostConnectivityCheck) {
    super(HelmCommandType.RELEASE_HISTORY, accountId, appId, kubeConfigLocation, commandName, activityId,
        containerServiceParams, releaseName, null, null, gitConfig, encryptedDataDetails, executionLogCallback,
        commandFlags, helmCommandFlag, null, helmVersion, ocPath, workingDir, variableOverridesYamlFiles, gitFileConfig,
        k8SteadyStateCheckEnabled, mergeCapabilities, isGitHostConnectivityCheck);
  }
}
