package software.wings.delegatetasks.spotinst.taskhandler;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.delegate.command.CommandExecutionResult.CommandExecutionStatus.FAILURE;
import static io.harness.delegate.command.CommandExecutionResult.CommandExecutionStatus.SUCCESS;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.google.inject.Singleton;

import com.amazonaws.services.ec2.model.Instance;
import io.harness.delegate.task.spotinst.request.SpotInstDeployTaskParameters;
import io.harness.delegate.task.spotinst.request.SpotInstTaskParameters;
import io.harness.delegate.task.spotinst.response.SpotInstDeployTaskResponse;
import io.harness.delegate.task.spotinst.response.SpotInstTaskExecutionResponse;
import io.harness.spotinst.model.ElastiGroup;
import io.harness.spotinst.model.ElastiGroupInstanceHealth;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.wings.beans.AwsConfig;
import software.wings.beans.SpotInstConfig;
import software.wings.beans.command.ExecutionLogCallback;

import java.util.List;

@Slf4j
@Singleton
@NoArgsConstructor
public class SpotInstDeployTaskHandler extends SpotInstTaskHandler {
  protected SpotInstTaskExecutionResponse executeTaskInternal(SpotInstTaskParameters spotInstTaskParameters,
      ExecutionLogCallback logCallback, SpotInstConfig spotInstConfig, AwsConfig awsConfig) throws Exception {
    if (!(spotInstTaskParameters instanceof SpotInstDeployTaskParameters)) {
      String message =
          format("Parameters of unrecognized class: [%s] found while executing setup step. Workflow execution: [%s]",
              spotInstTaskParameters.getClass().getSimpleName(), spotInstTaskParameters.getWorkflowExecutionId());
      logger.error(message);
      return SpotInstTaskExecutionResponse.builder().commandExecutionStatus(FAILURE).errorMessage(message).build();
    }

    String spotInstAccountId = spotInstConfig.getSpotInstAccountId();
    String spotInstToken = String.valueOf(spotInstConfig.getSpotInstToken());
    SpotInstDeployTaskParameters deployTaskParameters = (SpotInstDeployTaskParameters) spotInstTaskParameters;
    ElastiGroup newElastiGroup = deployTaskParameters.getNewElastiGroupWithUpdatedCapacity();
    ElastiGroup oldElastiGroup = deployTaskParameters.getOldElastiGroupWithUpdatedCapacity();
    boolean resizeNewFirst = deployTaskParameters.isResizeNewFirst();
    int steadyStateTimeOut = getTimeOut(deployTaskParameters.getSteadyStateTimeOut());

    if (resizeNewFirst) {
      if (newElastiGroup != null) {
        updateElastiGroupAndWait(spotInstToken, spotInstAccountId, newElastiGroup, logCallback,
            deployTaskParameters.getWorkflowExecutionId(), steadyStateTimeOut);
      }
      if (oldElastiGroup != null) {
        updateElastiGroupAndWait(spotInstToken, spotInstAccountId, oldElastiGroup, logCallback,
            deployTaskParameters.getWorkflowExecutionId(), steadyStateTimeOut);
      }
    } else {
      if (oldElastiGroup != null) {
        updateElastiGroupAndWait(spotInstToken, spotInstAccountId, oldElastiGroup, logCallback,
            deployTaskParameters.getWorkflowExecutionId(), steadyStateTimeOut);
      }
      if (newElastiGroup != null) {
        updateElastiGroupAndWait(spotInstToken, spotInstAccountId, newElastiGroup, logCallback,
            deployTaskParameters.getWorkflowExecutionId(), steadyStateTimeOut);
      }
    }

    List<Instance> newElastiGroupInstances = newElastiGroup != null
        ? getAllEc2InstancesOfElastiGroup(awsConfig, deployTaskParameters.getAwsRegion(), spotInstToken,
              spotInstAccountId, newElastiGroup.getId(), logCallback)
        : emptyList();

    return SpotInstTaskExecutionResponse.builder()
        .commandExecutionStatus(SUCCESS)
        .spotInstTaskResponse(SpotInstDeployTaskResponse.builder().ec2InstancesAdded(newElastiGroupInstances).build())
        .build();
  }

  private List<Instance> getAllEc2InstancesOfElastiGroup(AwsConfig awsConfig, String awsRegion, String spotInstToken,
      String spotInstAccountId, String elastiGroupId, ExecutionLogCallback logCallback) throws Exception {
    logCallback.saveExecutionLog(format("Querying to get all the instances for elasti group: [%s]", elastiGroupId));
    List<ElastiGroupInstanceHealth> instanceHealths =
        spotInstHelperServiceDelegate.listElastiGroupInstancesHealth(spotInstToken, spotInstAccountId, elastiGroupId);
    if (isEmpty(instanceHealths)) {
      return emptyList();
    }
    List<String> instanceIds = instanceHealths.stream().map(ElastiGroupInstanceHealth::getInstanceId).collect(toList());
    return awsEc2HelperServiceDelegate.listEc2Instances(awsConfig, emptyList(), instanceIds, awsRegion);
  }
}