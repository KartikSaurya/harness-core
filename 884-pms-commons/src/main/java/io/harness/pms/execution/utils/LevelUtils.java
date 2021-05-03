package io.harness.pms.execution.utils;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.plan.PlanNodeProto;
import io.harness.pms.contracts.steps.StepType;

@OwnedBy(PIPELINE)
public class LevelUtils {
  public static Level buildLevelFromPlanNode(String runtimeId, PlanNodeProto node) {
    return buildLevelFromPlanNode(runtimeId, 0, node);
  }

  public static Level buildLevelFromPlanNode(String runtimeId, int retryIndex, PlanNodeProto node) {
    Level.Builder levelBuilder = Level.newBuilder()
                                     .setSetupId(node.getUuid())
                                     .setRuntimeId(runtimeId)
                                     .setIdentifier(node.getIdentifier())
                                     .setRetryIndex(retryIndex)
                                     .setStepType(StepType.newBuilder().setType(node.getStepType().getType()).build());
    if (node.getGroup() != null) {
      levelBuilder.setGroup(node.getGroup());
    }
    return levelBuilder.build();
  }
}
