/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.api;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.container.ContainerInfo;
import io.harness.delegate.task.helm.HelmChartInfo;

import software.wings.beans.container.Label;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@OwnedBy(CDP)
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ContainerDeploymentInfoWithLabels extends BaseContainerDeploymentInfo {
  private List<Label> labels;
  private String newVersion;
  @Deprecated private String namespace;
  private List<String> namespaces;
  private HelmChartInfo helmChartInfo;
  private List<ContainerInfo> containerInfoList;
  /*
   *   Helm Release to which the kubernetes pods belong to
   */
  private String releaseName;

  @Builder
  public ContainerDeploymentInfoWithLabels(String clusterName, List<Label> labels, String newVersion, String namespace,
      HelmChartInfo helmChartInfo, List<ContainerInfo> containerInfoList, String releaseName, List<String> namespaces) {
    super(clusterName);
    this.labels = labels;
    this.newVersion = newVersion;
    this.namespace = namespace;
    this.helmChartInfo = helmChartInfo;
    this.containerInfoList = containerInfoList;
    this.releaseName = releaseName;
    this.namespaces = namespaces;
  }
}
