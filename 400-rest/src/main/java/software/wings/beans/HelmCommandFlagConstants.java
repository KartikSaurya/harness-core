/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import static software.wings.beans.appmanifest.StoreType.HelmChartRepo;
import static software.wings.beans.appmanifest.StoreType.HelmSourceRepo;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.helm.HelmSubCommandType;
import io.harness.k8s.model.HelmVersion;

import software.wings.api.DeploymentType;
import software.wings.beans.appmanifest.StoreType;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@OwnedBy(CDP)
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
public final class HelmCommandFlagConstants {
  @Getter
  public enum HelmSubCommand {
    INSTALL(HelmSubCommandType.INSTALL, ImmutableSet.of(DeploymentType.HELM),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    UPGRADE(HelmSubCommandType.UPGRADE, ImmutableSet.of(DeploymentType.HELM),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    ROLLBACK(HelmSubCommandType.ROLLBACK, ImmutableSet.of(DeploymentType.HELM),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    HISTORY(HelmSubCommandType.HISTORY, ImmutableSet.of(DeploymentType.HELM),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    DELETE(HelmSubCommandType.DELETE, ImmutableSet.of(DeploymentType.HELM),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    UNINSTALL(HelmSubCommandType.UNINSTALL, ImmutableSet.of(DeploymentType.HELM),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    LIST(HelmSubCommandType.LIST, ImmutableSet.of(DeploymentType.HELM), ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    VERSION(HelmSubCommandType.VERSION, ImmutableSet.of(DeploymentType.HELM, DeploymentType.KUBERNETES),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo)),
    PULL(HelmSubCommandType.PULL, ImmutableSet.of(DeploymentType.HELM, DeploymentType.KUBERNETES),
        ImmutableSet.of(HelmChartRepo)),
    FETCH(HelmSubCommandType.FETCH, ImmutableSet.of(DeploymentType.HELM, DeploymentType.KUBERNETES),
        ImmutableSet.of(HelmChartRepo)),
    TEMPLATE(HelmSubCommandType.TEMPLATE, ImmutableSet.of(DeploymentType.HELM, DeploymentType.KUBERNETES),
        ImmutableSet.of(HelmSourceRepo, HelmChartRepo));

    private final HelmSubCommandType subCommandType;
    private final Set<DeploymentType> deploymentTypes;
    private final Set<StoreType> storeTypes;

    HelmSubCommand(HelmSubCommandType subCommandType, Set<DeploymentType> deploymentTypes, Set<StoreType> storeTypes) {
      this.subCommandType = subCommandType;
      this.deploymentTypes = deploymentTypes;
      this.storeTypes = storeTypes;
    }
  }

  public static Set<HelmSubCommand> getFilteredHelmSubCommands(
      HelmVersion version, DeploymentType deploymentType, StoreType storeType) {
    return Arrays.stream(HelmSubCommand.values())
        .filter(sc
            -> sc.getSubCommandType().getHelmVersions().contains(version)
                && sc.getDeploymentTypes().contains(deploymentType) && sc.getStoreTypes().contains(storeType))
        .collect(Collectors.toSet());
  }

  public static Set<HelmSubCommand> getHelmSubCommands(HelmVersion version) {
    return Arrays.stream(HelmSubCommand.values())
        .filter(sc -> sc.getSubCommandType().getHelmVersions().contains(version))
        .collect(Collectors.toSet());
  }
}
