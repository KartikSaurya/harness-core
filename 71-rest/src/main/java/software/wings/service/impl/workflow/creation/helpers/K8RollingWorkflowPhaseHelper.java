package software.wings.service.impl.workflow.creation.helpers;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static software.wings.beans.PhaseStep.PhaseStepBuilder.aPhaseStep;
import static software.wings.beans.PhaseStepType.K8S_PHASE_STEP;
import static software.wings.sm.StateType.K8S_DEPLOYMENT_ROLLING;
import static software.wings.sm.StateType.K8S_DEPLOYMENT_ROLLING_ROLLBACK;

import io.harness.beans.ExecutionStatus;
import software.wings.beans.GraphNode;
import software.wings.beans.PhaseStep;
import software.wings.common.WorkflowConstants;
import software.wings.service.impl.workflow.WorkflowServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class K8RollingWorkflowPhaseHelper extends PhaseHelper {
  // Get all Rolling Steps
  public List<PhaseStep> getWorkflowPhaseSteps() {
    List<PhaseStep> phaseSteps = new ArrayList<>();
    phaseSteps.add(getRollingDeployPhaseStep());
    phaseSteps.add(getRollingVerifyPhaseStep());
    phaseSteps.add(getRollingWrapUpPhaseStep());
    return phaseSteps;
  }

  // Get all Rolling Rollback Steps
  public List<PhaseStep> getRollbackPhaseSteps() {
    List<PhaseStep> phaseSteps = new ArrayList<>();
    phaseSteps.add(getRollingRollbackDeployPhaseStep());
    phaseSteps.add(getRollingRollbackWrapUpPhaseStep());
    return phaseSteps;
  }

  // Steps for Rolling

  private PhaseStep getRollingDeployPhaseStep() {
    return aPhaseStep(K8S_PHASE_STEP, WorkflowServiceHelper.DEPLOY)
        .addStep(GraphNode.builder()
                     .id(generateUuid())
                     .type(K8S_DEPLOYMENT_ROLLING.name())
                     .name(WorkflowConstants.K8S_DEPLOYMENT_ROLLING)
                     .properties(new HashMap<>())
                     .build())
        .build();
  }

  private PhaseStep getRollingVerifyPhaseStep() {
    return aPhaseStep(K8S_PHASE_STEP, "Verify").build();
  }

  private PhaseStep getRollingWrapUpPhaseStep() {
    return aPhaseStep(K8S_PHASE_STEP, WorkflowServiceHelper.WRAP_UP).build();
  }

  // Steps for Rolling Rollback

  private PhaseStep getRollingRollbackDeployPhaseStep() {
    return aPhaseStep(K8S_PHASE_STEP, WorkflowServiceHelper.DEPLOY)
        .addStep(GraphNode.builder()
                     .id(generateUuid())
                     .type(K8S_DEPLOYMENT_ROLLING_ROLLBACK.name())
                     .name(WorkflowConstants.K8S_DEPLOYMENT_ROLLING_ROLLBACK)
                     .rollback(true)
                     .build())
        .withPhaseStepNameForRollback(WorkflowServiceHelper.DEPLOY)
        .withStatusForRollback(ExecutionStatus.SUCCESS)
        .withRollback(true)
        .build();
  }

  private PhaseStep getRollingRollbackWrapUpPhaseStep() {
    return aPhaseStep(K8S_PHASE_STEP, WorkflowServiceHelper.WRAP_UP)
        .withPhaseStepNameForRollback(WorkflowServiceHelper.WRAP_UP)
        .withRollback(true)
        .build();
  }
}
