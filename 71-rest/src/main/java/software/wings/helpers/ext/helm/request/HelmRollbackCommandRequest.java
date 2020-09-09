package software.wings.helpers.ext.helm.request;

import io.harness.k8s.model.HelmVersion;
import io.harness.logging.LogCallback;
import io.harness.security.encryption.EncryptedDataDetail;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import software.wings.beans.GitConfig;
import software.wings.beans.GitFileConfig;
import software.wings.beans.container.HelmChartSpecification;
import software.wings.helpers.ext.k8s.request.K8sDelegateManifestConfig;
import software.wings.service.impl.ContainerServiceParams;

import java.util.List;

/**
 * Created by anubhaw on 3/22/18.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HelmRollbackCommandRequest extends HelmCommandRequest {
  private Integer newReleaseVersion;
  private Integer prevReleaseVersion;
  private Integer rollbackVersion;
  private long timeoutInMillis;

  public HelmRollbackCommandRequest() {
    super(HelmCommandType.ROLLBACK);
  }

  @Builder
  public HelmRollbackCommandRequest(String accountId, String appId, String kubeConfigLocation, String commandName,
      String activityId, ContainerServiceParams containerServiceParams, String releaseName, int newReleaseVersion,
      int prevReleaseVersion, int rollbackVersion, long timeoutInMillis, HelmChartSpecification chartSpecification,
      String repoName, GitConfig gitConfig, List<EncryptedDataDetail> encryptedDataDetails,
      LogCallback executionLogCallback, String commandFlags, K8sDelegateManifestConfig sourceRepoConfig,
      HelmVersion helmVersion, String ocPath, String workingDir, GitFileConfig gitFileConfig,
      List<String> variableOverridesYamlFiles, boolean k8SteadyStateCheckEnabled, boolean deprecateFabric8Enabled) {
    super(HelmCommandType.ROLLBACK, accountId, appId, kubeConfigLocation, commandName, activityId,
        containerServiceParams, releaseName, chartSpecification, repoName, gitConfig, encryptedDataDetails,
        executionLogCallback, commandFlags, sourceRepoConfig, helmVersion, ocPath, workingDir,
        variableOverridesYamlFiles, gitFileConfig, k8SteadyStateCheckEnabled, deprecateFabric8Enabled);
    this.newReleaseVersion = newReleaseVersion;
    this.prevReleaseVersion = prevReleaseVersion;
    this.rollbackVersion = rollbackVersion;
    this.timeoutInMillis = timeoutInMillis;
  }
}
