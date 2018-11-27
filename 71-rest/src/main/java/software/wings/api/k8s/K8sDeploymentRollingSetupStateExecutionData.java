package software.wings.api.k8s;

import com.google.common.collect.Maps;

import io.harness.delegate.task.protocol.ResponseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.wings.api.ExecutionDataValue;
import software.wings.sm.StateExecutionData;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class K8sDeploymentRollingSetupStateExecutionData extends StateExecutionData implements ResponseData {
  private String activityId;
  private String releaseName;
  private String namespace;
  private String clusterName;
  private Integer releaseNumber;
  private String commandName;

  @Override
  public Map<String, ExecutionDataValue> getExecutionDetails() {
    return getInternalExecutionDetails();
  }

  @Override
  public Map<String, ExecutionDataValue> getExecutionSummary() {
    return getInternalExecutionDetails();
  }

  private Map<String, ExecutionDataValue> getInternalExecutionDetails() {
    Map<String, ExecutionDataValue> executionDetails = Maps.newLinkedHashMap();
    // putting activityId is very important, as without it UI wont make call to fetch commandLogs that are shown
    // in activity window
    putNotNull(executionDetails, "activityId",
        ExecutionDataValue.builder().value(activityId).displayName("Activity Id").build());
    putNotNull(executionDetails, "cluster",
        ExecutionDataValue.builder().value(clusterName).displayName("Cluster Name").build());
    putNotNull(
        executionDetails, "namespace", ExecutionDataValue.builder().value(namespace).displayName("Namespace").build());
    putNotNull(executionDetails, "releaseName",
        ExecutionDataValue.builder().value(releaseName).displayName("Release Name").build());
    putNotNull(executionDetails, "releaseNumber",
        ExecutionDataValue.builder().value(releaseNumber).displayName("Release Number").build());

    return executionDetails;
  }

  @Override
  public K8sDeployRollingSetupExecutionSummary getStepExecutionSummary() {
    return K8sDeployRollingSetupExecutionSummary.builder()
        .releaseName(releaseName)
        .releaseNumber(releaseNumber)
        .build();
  }
}
