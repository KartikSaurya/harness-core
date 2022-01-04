package io.harness.cdng.creator.plan.steps;

import io.harness.cdng.k8s.K8sBlueGreenStepNode;
import io.harness.executions.steps.StepSpecTypeConstants;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationContext;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationResponse;

import com.google.common.collect.Sets;
import java.util.Set;

public class K8sBlueGreenStepPlanCreator extends CDPMSStepPlanCreatorV2<K8sBlueGreenStepNode> {
  @Override
  public Set<String> getSupportedStepTypes() {
    return Sets.newHashSet(StepSpecTypeConstants.K8S_BLUE_GREEN_DEPLOY);
  }

  @Override
  public Class<K8sBlueGreenStepNode> getFieldClass() {
    return K8sBlueGreenStepNode.class;
  }

  @Override
  public PlanCreationResponse createPlanForField(PlanCreationContext ctx, K8sBlueGreenStepNode stepElement) {
    return super.createPlanForField(ctx, stepElement);
  }
}
