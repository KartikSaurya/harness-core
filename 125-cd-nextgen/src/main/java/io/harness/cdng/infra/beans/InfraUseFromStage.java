package io.harness.cdng.infra.beans;

import io.harness.cdng.environment.yaml.EnvironmentYaml;
import io.harness.cdng.infra.InfrastructureDef;
import io.harness.cdng.visitor.LevelNodeQualifierName;
import io.harness.cdng.visitor.helpers.pipelineinfrastructure.InfraUseFromOverridesVisitorHelper;
import io.harness.cdng.visitor.helpers.pipelineinfrastructure.InfraUseFromStageVisitorHelper;
import io.harness.walktree.beans.LevelNode;
import io.harness.walktree.beans.VisitableChildren;
import io.harness.walktree.visitor.SimpleVisitorHelper;
import io.harness.walktree.visitor.Visitable;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

@Data
@Builder
@SimpleVisitorHelper(helperClass = InfraUseFromStageVisitorHelper.class)
public class InfraUseFromStage implements Serializable, Visitable {
  // Stage identifier of the stage to select from.
  @NotNull String stage;
  Overrides overrides;

  // For Visitor Framework Impl
  String metadata;

  @Override
  public VisitableChildren getChildrenToWalk() {
    VisitableChildren children = VisitableChildren.builder().build();
    children.add("overrides", overrides);
    return children;
  }

  @Override
  public LevelNode getLevelNode() {
    return LevelNode.builder().qualifierName(LevelNodeQualifierName.INFRA_USE_FROM_STAGE).build();
  }

  @Data
  @Builder
  @ApiModel(value = "InfraOverrides")
  @SimpleVisitorHelper(helperClass = InfraUseFromOverridesVisitorHelper.class)
  public static class Overrides implements Serializable, Visitable {
    EnvironmentYaml environment;
    InfrastructureDef infrastructureDefinition;

    @Override
    public VisitableChildren getChildrenToWalk() {
      VisitableChildren children = VisitableChildren.builder().build();
      children.add("infrastructureDefinition", infrastructureDefinition);
      children.add("environment", environment);
      return children;
    }

    @Override
    public LevelNode getLevelNode() {
      return LevelNode.builder().qualifierName(LevelNodeQualifierName.INFRA_USE_FROM_STAGE_OVERRIDES).build();
    }
  }
}
