/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.k8s;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.pipeline.CDStepInfo;
import io.harness.cdng.visitor.helpers.cdstepinfo.K8sRollingRollbackStepInfoVisitorHelper;
import io.harness.executions.steps.StepSpecTypeConstants;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.plancreator.steps.common.SpecParameters;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.OrchestrationFacilitatorType;
import io.harness.pms.yaml.ParameterField;
import io.harness.walktree.visitor.SimpleVisitorHelper;
import io.harness.walktree.visitor.Visitable;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(CDP)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(StepSpecTypeConstants.K8S_ROLLING_ROLLBACK)
@SimpleVisitorHelper(helperClass = K8sRollingRollbackStepInfoVisitorHelper.class)
@TypeAlias("k8sRollingRollback")
@RecasterAlias("io.harness.cdng.k8s.K8sRollingRollbackStepInfo")
public class K8sRollingRollbackStepInfo extends K8sRollingRollbackBaseStepInfo implements CDStepInfo, Visitable {
  // For Visitor Framework Impl
  @Getter(onMethod_ = { @ApiModelProperty(hidden = true) }) @ApiModelProperty(hidden = true) String metadata;

  @Builder(builderMethodName = "infoBuilder")
  public K8sRollingRollbackStepInfo(ParameterField<Boolean> skipDryRun,
      ParameterField<List<TaskSelectorYaml>> delegateSelectors, String rollingStepFqn) {
    super(skipDryRun, delegateSelectors, rollingStepFqn);
  }

  @Override
  public StepType getStepType() {
    return K8sRollingRollbackStep.STEP_TYPE;
  }

  @Override
  public String getFacilitatorType() {
    return OrchestrationFacilitatorType.TASK;
  }

  @Override
  public SpecParameters getSpecParameters() {
    return K8sRollingRollbackStepParameters.infoBuilder()
        .skipDryRun(skipDryRun)
        .delegateSelectors(delegateSelectors)
        .rollingStepFqn(rollingStepFqn)
        .build();
  }
}
