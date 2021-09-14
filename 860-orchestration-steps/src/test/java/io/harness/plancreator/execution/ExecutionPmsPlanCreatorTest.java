/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.plancreator.execution;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.rule.OwnerRule.NAMAN;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.sdk.core.plan.PlanNode;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationContext;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationResponse;
import io.harness.pms.yaml.YamlField;
import io.harness.pms.yaml.YamlNode;
import io.harness.pms.yaml.YamlUtils;
import io.harness.rule.Owner;
import io.harness.steps.common.NGSectionStepParameters;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(PIPELINE)
public class ExecutionPmsPlanCreatorTest {
  YamlField executionYamlField;
  ExecutionElementConfig executionElementConfig;
  PlanCreationContext context;

  @Before
  public void setUp() throws IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    final URL testFile = classLoader.getResource("complex_pipeline.yaml");
    assertThat(testFile).isNotNull();
    String pipelineYaml = Resources.toString(testFile, Charsets.UTF_8);
    String pipelineYamlWithUuid = YamlUtils.injectUuid(pipelineYaml);

    YamlField pipelineYamlField = YamlUtils.readTree(pipelineYamlWithUuid).getNode().getField("pipeline");
    assertThat(pipelineYamlField).isNotNull();
    YamlField stagesYamlField = pipelineYamlField.getNode().getField("stages");
    assertThat(stagesYamlField).isNotNull();
    List<YamlNode> stagesNodes = stagesYamlField.getNode().asArray();
    YamlField approvalStageField = stagesNodes.get(0).getField("stage");
    YamlField approvalSpecField = Objects.requireNonNull(approvalStageField).getNode().getField("spec");
    executionYamlField = Objects.requireNonNull(approvalSpecField).getNode().getField("execution");
    assertThat(executionYamlField).isNotNull();
    executionElementConfig = YamlUtils.read(executionYamlField.getNode().toString(), ExecutionElementConfig.class);

    context = PlanCreationContext.builder().currentField(executionYamlField).build();
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testCreatePlanForChildrenNodes() {
    YamlField stepsField = executionYamlField.getNode().getField("steps");
    assertThat(stepsField).isNotNull();
    YamlField stepGroupField = stepsField.getNode().asArray().get(0).getField("stepGroup");
    assertThat(stepGroupField).isNotNull();
    YamlField parallelField = stepsField.getNode().asArray().get(1).getField("parallel");
    assertThat(parallelField).isNotNull();

    ExecutionPmsPlanCreator executionPmsPlanCreator = new ExecutionPmsPlanCreator();
    LinkedHashMap<String, PlanCreationResponse> planForChildrenNodes =
        executionPmsPlanCreator.createPlanForChildrenNodes(context, executionElementConfig);
    assertThat(planForChildrenNodes).hasSize(3);

    assertThat(planForChildrenNodes.containsKey(stepsField.getNode().getUuid())).isTrue();
    PlanCreationResponse stepsResponse = planForChildrenNodes.get(stepsField.getNode().getUuid());
    assertThat(stepsResponse.getDependencies()).isNull();
    assertThat(stepsResponse.getNodes()).hasSize(1);
    assertThat(stepsResponse.getNodes().containsKey(stepsField.getNode().getUuid())).isTrue();

    assertThat(planForChildrenNodes.containsKey(stepGroupField.getNode().getUuid())).isTrue();
    PlanCreationResponse stepGroupResponse = planForChildrenNodes.get(stepGroupField.getNode().getUuid());
    assertThat(stepGroupResponse.getNodes()).hasSize(0);
    assertThat(stepGroupResponse.getDependencies().getDependenciesMap()).hasSize(1);
    assertThat(stepGroupResponse.getDependencies().getDependenciesMap().containsKey(stepGroupField.getNode().getUuid()))
        .isTrue();
    assertThat(stepGroupResponse.getDependencies().getDependenciesMap().get(stepGroupField.getNode().getUuid()))
        .isEqualTo("pipeline/stages/[0]/stage/spec/execution/steps/[0]/stepGroup");

    assertThat(planForChildrenNodes.containsKey(parallelField.getNode().getUuid())).isTrue();
    PlanCreationResponse parallelResponse = planForChildrenNodes.get(parallelField.getNode().getUuid());
    assertThat(parallelResponse.getNodes()).hasSize(0);
    assertThat(parallelResponse.getDependencies().getDependenciesMap()).hasSize(1);
    assertThat(parallelResponse.getDependencies().getDependenciesMap().containsKey(parallelField.getNode().getUuid()))
        .isTrue();
    assertThat(parallelResponse.getDependencies().getDependenciesMap().get(parallelField.getNode().getUuid()))
        .isEqualTo("pipeline/stages/[0]/stage/spec/execution/steps/[1]/parallel");
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testCreatePlanForParentNode() {
    YamlField stepsField = executionYamlField.getNode().getField("steps");
    assertThat(stepsField).isNotNull();

    ExecutionPmsPlanCreator executionPmsPlanCreator = new ExecutionPmsPlanCreator();
    PlanNode planForParentNode = executionPmsPlanCreator.createPlanForParentNode(context, executionElementConfig, null);
    assertThat(planForParentNode.getUuid()).isEqualTo(executionYamlField.getNode().getUuid());
    assertThat(planForParentNode.getIdentifier()).isEqualTo("execution");
    assertThat(planForParentNode.getStepType())
        .isEqualTo(StepType.newBuilder().setType("NG_SECTION").setStepCategory(StepCategory.STEP).build());
    assertThat(planForParentNode.getGroup()).isEqualTo("EXECUTION");
    assertThat(planForParentNode.getName()).isEqualTo("Execution");
    assertThat(planForParentNode.getFacilitatorObtainments()).hasSize(1);
    assertThat(planForParentNode.getFacilitatorObtainments().get(0).getType().getType()).isEqualTo("CHILD");
    assertThat(planForParentNode.isSkipExpressionChain()).isFalse();

    assertThat(planForParentNode.getStepParameters() instanceof NGSectionStepParameters).isTrue();
    NGSectionStepParameters stepParameters = (NGSectionStepParameters) planForParentNode.getStepParameters();
    assertThat(stepParameters.getChildNodeId()).isEqualTo(stepsField.getNode().getUuid());
    assertThat(stepParameters.getLogMessage()).isEqualTo("Execution Element");
  }
}
