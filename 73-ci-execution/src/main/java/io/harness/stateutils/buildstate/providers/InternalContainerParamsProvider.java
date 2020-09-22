package io.harness.stateutils.buildstate.providers;

import static io.harness.common.CIExecutionConstants.ADDON_ARGS;
import static io.harness.common.CIExecutionConstants.ADDON_CONTAINER_LIMIT_CPU;
import static io.harness.common.CIExecutionConstants.ADDON_CONTAINER_LIMIT_MEM;
import static io.harness.common.CIExecutionConstants.ADDON_CONTAINER_NAME;
import static io.harness.common.CIExecutionConstants.ADDON_CONTAINER_REQ_CPU;
import static io.harness.common.CIExecutionConstants.ADDON_CONTAINER_REQ_MEM;
import static io.harness.common.CIExecutionConstants.ADDON_JFROG_PATH;
import static io.harness.common.CIExecutionConstants.ADDON_JFROG_VARIABLE;
import static io.harness.common.CIExecutionConstants.ADDON_PATH;
import static io.harness.common.CIExecutionConstants.ADDON_PORT;
import static io.harness.common.CIExecutionConstants.ADDON_VOLUME;
import static io.harness.common.CIExecutionConstants.HARNESS_ACCOUNT_ID_VARIABLE;
import static io.harness.common.CIExecutionConstants.HARNESS_BUILD_ID_VARIABLE;
import static io.harness.common.CIExecutionConstants.HARNESS_ORG_ID_VARIABLE;
import static io.harness.common.CIExecutionConstants.HARNESS_PROJECT_ID_VARIABLE;
import static io.harness.common.CIExecutionConstants.HARNESS_STAGE_ID_VARIABLE;
import static io.harness.common.CIExecutionConstants.LITE_ENGINE_ARGS;
import static io.harness.common.CIExecutionConstants.LITE_ENGINE_CONTAINER_NAME;
import static io.harness.common.CIExecutionConstants.LOG_SERVICE_ENDPOINT_VARIABLE;
import static io.harness.common.CIExecutionConstants.LOG_SERVICE_ENDPOINT_VARIABLE_VALUE;
import static io.harness.common.CIExecutionConstants.SH_COMMAND;
import static io.harness.govern.Switch.unhandled;
import static io.harness.stateutils.buildstate.providers.InternalImageDetailsProvider.ImageKind.ADDON_IMAGE;
import static io.harness.stateutils.buildstate.providers.InternalImageDetailsProvider.ImageKind.LITE_ENGINE_IMAGE;
import static software.wings.common.CICommonPodConstants.MOUNT_PATH;
import static software.wings.common.CICommonPodConstants.STEP_EXEC;

import io.harness.beans.sweepingoutputs.K8PodDetails;
import lombok.experimental.UtilityClass;
import software.wings.beans.ci.pod.CIContainerType;
import software.wings.beans.ci.pod.CIK8ContainerParams;
import software.wings.beans.ci.pod.CIK8ContainerParams.CIK8ContainerParamsBuilder;
import software.wings.beans.ci.pod.ContainerResourceParams;
import software.wings.beans.ci.pod.ContainerSecrets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides container parameters for internally used containers
 */
@UtilityClass
// TODO: fetch constants from config file.
public class InternalContainerParamsProvider {
  public enum ContainerKind { ADDON_CONTAINER, LITE_ENGINE_CONTAINER }

  public CIK8ContainerParamsBuilder getContainerParams(ContainerKind kind, K8PodDetails k8PodDetails) {
    if (kind == null) {
      return null;
    }
    switch (kind) {
      case ADDON_CONTAINER:
        return getAddonContainerParams(k8PodDetails);
      case LITE_ENGINE_CONTAINER:
        return getLiteEngineContainerParams();
      default:
        unhandled(kind);
    }
    return null;
  }

  private CIK8ContainerParamsBuilder getLiteEngineContainerParams() {
    Map<String, String> map = new HashMap<>();
    map.put(STEP_EXEC, MOUNT_PATH);
    List<String> args = new ArrayList<>(Collections.singletonList(LITE_ENGINE_ARGS));
    // todo send correct container type
    return CIK8ContainerParams.builder()
        .name(LITE_ENGINE_CONTAINER_NAME)
        .containerType(CIContainerType.LITE_ENGINE)
        .imageDetailsWithConnector(InternalImageDetailsProvider.getImageDetails(LITE_ENGINE_IMAGE))
        .containerSecrets(ContainerSecrets.builder().build())
        .volumeToMountPath(map)
        .commands(SH_COMMAND)
        .args(args);
  }

  private CIK8ContainerParamsBuilder getAddonContainerParams(K8PodDetails k8PodDetails) {
    Map<String, String> map = new HashMap<>();
    map.put(STEP_EXEC, MOUNT_PATH);
    map.put(ADDON_VOLUME, ADDON_PATH);
    List<String> args = new ArrayList<>(Collections.singletonList(ADDON_ARGS));
    return CIK8ContainerParams.builder()
        .name(ADDON_CONTAINER_NAME)
        .containerResourceParams(getAddonResourceParams())
        .envVars(getAddonEnvVars(k8PodDetails))
        .containerType(CIContainerType.ADD_ON)
        .imageDetailsWithConnector(InternalImageDetailsProvider.getImageDetails(ADDON_IMAGE))
        .volumeToMountPath(map)
        .commands(SH_COMMAND)
        .args(args)
        .ports(Collections.singletonList(ADDON_PORT));
  }

  private Map<String, String> getAddonEnvVars(K8PodDetails k8PodDetails) {
    Map<String, String> envVars = new HashMap<>();
    final String accountID = k8PodDetails.getBuildNumber().getAccountIdentifier();
    final String projectID = k8PodDetails.getBuildNumber().getProjectIdentifier();
    final String orgID = k8PodDetails.getBuildNumber().getOrgIdentifier();
    final Long buildNumber = k8PodDetails.getBuildNumber().getBuildNumber();
    final String stageID = k8PodDetails.getStageID();
    envVars.put(HARNESS_ACCOUNT_ID_VARIABLE, accountID);
    envVars.put(HARNESS_PROJECT_ID_VARIABLE, projectID);
    envVars.put(HARNESS_ORG_ID_VARIABLE, orgID);
    envVars.put(HARNESS_BUILD_ID_VARIABLE, buildNumber.toString());
    envVars.put(HARNESS_STAGE_ID_VARIABLE, stageID);
    envVars.put(ADDON_JFROG_VARIABLE, ADDON_JFROG_PATH);
    envVars.put(LOG_SERVICE_ENDPOINT_VARIABLE, LOG_SERVICE_ENDPOINT_VARIABLE_VALUE);
    return envVars;
  }

  private ContainerResourceParams getAddonResourceParams() {
    return ContainerResourceParams.builder()
        .resourceRequestMilliCpu(ADDON_CONTAINER_REQ_CPU)
        .resourceRequestMemoryMiB(ADDON_CONTAINER_REQ_MEM)
        .resourceLimitMilliCpu(ADDON_CONTAINER_LIMIT_CPU)
        .resourceLimitMemoryMiB(ADDON_CONTAINER_LIMIT_MEM)
        .build();
  }
}
