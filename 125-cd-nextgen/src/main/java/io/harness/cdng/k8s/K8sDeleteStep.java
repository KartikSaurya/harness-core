package io.harness.cdng.k8s;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.infra.beans.InfrastructureOutcome;
import io.harness.cdng.manifest.yaml.ManifestOutcome;
import io.harness.delegate.beans.ErrorNotifyResponseData;
import io.harness.delegate.task.k8s.DeleteResourcesType;
import io.harness.delegate.task.k8s.K8sDeleteRequest;
import io.harness.delegate.task.k8s.K8sDeployResponse;
import io.harness.delegate.task.k8s.K8sTaskType;
import io.harness.exception.InvalidRequestException;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.logging.CommandExecutionStatus;
import io.harness.ngpipeline.common.AmbianceHelper;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.sdk.core.execution.ErrorDataException;
import io.harness.pms.sdk.core.resolver.outcome.OutcomeService;
import io.harness.pms.sdk.core.steps.executables.TaskChainExecutable;
import io.harness.pms.sdk.core.steps.executables.TaskChainResponse;
import io.harness.pms.sdk.core.steps.io.PassThroughData;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepResponseBuilder;
import io.harness.tasks.ResponseData;

import com.google.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

@OwnedBy(HarnessTeam.CDP)
public class K8sDeleteStep implements TaskChainExecutable<K8sDeleteStepParameters>, K8sStepExecutor {
  public static final StepType STEP_TYPE =
      StepType.newBuilder().setType(ExecutionNodeType.K8S_DELETE.getYamlType()).build();
  public static final String K8S_DELETE_COMMAND_NAME = "Delete";

  @Inject private OutcomeService outcomeService;
  @Inject private K8sStepHelper k8sStepHelper;

  @Override
  public TaskChainResponse startChainLink(
      Ambiance ambiance, K8sDeleteStepParameters stepParameters, StepInputPackage inputPackage) {
    validate(stepParameters);
    return k8sStepHelper.startChainLink(this, ambiance, stepParameters);
  }

  private void validate(K8sDeleteStepParameters stepParameters) {
    if (stepParameters.getDeleteResources() == null) {
      throw new InvalidRequestException("DeleteResources is mandatory");
    }

    if (stepParameters.getDeleteResources().getType() == null) {
      throw new InvalidRequestException("DeleteResources type is mandatory");
    }

    if (stepParameters.getDeleteResources().getSpec() == null) {
      throw new InvalidRequestException("DeleteResources spec is mandatory");
    }
  }

  @Override
  public TaskChainResponse executeK8sTask(ManifestOutcome k8sManifestOutcome, Ambiance ambiance,
      K8sStepParameters stepParameters, List<String> valuesFileContents, InfrastructureOutcome infrastructure) {
    K8sDeleteStepParameters deleteStepParameters = (K8sDeleteStepParameters) stepParameters;
    boolean isResourceName = io.harness.delegate.task.k8s.DeleteResourcesType.ResourceName
        == deleteStepParameters.getDeleteResources().getType();
    boolean isManifestFiles = DeleteResourcesType.ManifestPath == deleteStepParameters.getDeleteResources().getType();

    String releaseName = k8sStepHelper.getReleaseName(infrastructure);
    final String accountId = AmbianceHelper.getAccountId(ambiance);

    K8sDeleteRequest request =
        K8sDeleteRequest.builder()
            .accountId(accountId)
            .releaseName(releaseName)
            .commandName(K8S_DELETE_COMMAND_NAME)
            .deleteResourcesType(deleteStepParameters.getDeleteResources().getType())
            .resources(isResourceName ? deleteStepParameters.getDeleteResources().getSpec().getResourceNames() : "")
            .deleteNamespacesForRelease(deleteStepParameters.deleteResources.getSpec().getDeleteNamespace())
            .filePaths(isManifestFiles ? deleteStepParameters.getDeleteResources().getSpec().getManifestPaths() : "")
            .valuesYamlList(k8sStepHelper.renderValues(k8sManifestOutcome, ambiance, valuesFileContents))
            .taskType(K8sTaskType.DELETE)
            .timeoutIntervalInMin(K8sStepHelper.getTimeout(stepParameters))
            .k8sInfraDelegateConfig(k8sStepHelper.getK8sInfraDelegateConfig(infrastructure, ambiance))
            .manifestDelegateConfig(k8sStepHelper.getManifestDelegateConfig(k8sManifestOutcome, ambiance))
            .build();

    return k8sStepHelper.queueK8sTask(stepParameters, request, ambiance, infrastructure);
  }

  @Override
  public TaskChainResponse executeNextLink(Ambiance ambiance, K8sDeleteStepParameters stepParameters,
      StepInputPackage inputPackage, PassThroughData passThroughData, Supplier<ResponseData> responseSupplier) {
    return k8sStepHelper.executeNextLink(this, ambiance, stepParameters, passThroughData, responseSupplier);
  }

  @Override
  public StepResponse finalizeExecution(Ambiance ambiance, K8sDeleteStepParameters stepParameters,
      PassThroughData passThroughData, Supplier<ResponseData> responseDataSupplier) {
    try {
      K8sDeployResponse k8sTaskExecutionResponse = (K8sDeployResponse) responseDataSupplier.get();
      StepResponseBuilder stepResponseBuilder = StepResponse.builder().unitProgressList(
          k8sTaskExecutionResponse.getCommandUnitsProgress().getUnitProgresses());

      if (k8sTaskExecutionResponse.getCommandExecutionStatus() != CommandExecutionStatus.SUCCESS) {
        return K8sStepHelper.getFailureResponseBuilder(stepParameters, k8sTaskExecutionResponse, stepResponseBuilder)
            .build();
      }

      return stepResponseBuilder.status(Status.SUCCEEDED).build();
    } catch (ErrorDataException ex) {
      return K8sStepHelper
          .getDelegateErrorFailureResponseBuilder(stepParameters, (ErrorNotifyResponseData) ex.getErrorResponseData())
          .build();
    }
  }

  @Override
  public Class<K8sDeleteStepParameters> getStepParametersClass() {
    return K8sDeleteStepParameters.class;
  }
}
