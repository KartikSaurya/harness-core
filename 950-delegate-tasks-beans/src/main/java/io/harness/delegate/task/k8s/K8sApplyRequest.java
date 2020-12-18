package io.harness.delegate.task.k8s;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class K8sApplyRequest implements K8sDeployRequest {
  boolean skipDryRun;
  String releaseName;
  String commandName;
  K8sTaskType taskType;
  Integer timeoutIntervalInMin;
  List<String> valuesYamlList;
  K8sInfraDelegateConfig k8sInfraDelegateConfig;
  ManifestDelegateConfig manifestDelegateConfig;
  boolean deprecateFabric8Enabled;
  String accountId;
  boolean skipSteadyStateCheck;
  List<String> filePaths;
}