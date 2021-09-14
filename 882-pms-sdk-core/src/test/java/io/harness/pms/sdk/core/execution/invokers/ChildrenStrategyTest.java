/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.sdk.core.execution.invokers;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.PRASHANT;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.ChildrenExecutableResponse;
import io.harness.pms.contracts.execution.ChildrenExecutableResponse.Child;
import io.harness.pms.contracts.execution.ExecutionMode;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.events.SpawnChildrenRequest;
import io.harness.pms.contracts.steps.io.StepResponseProto;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.plan.execution.SetupAbstractionKeys;
import io.harness.pms.sdk.core.PmsSdkCoreTestBase;
import io.harness.pms.sdk.core.execution.InvokerPackage;
import io.harness.pms.sdk.core.execution.ResumePackage;
import io.harness.pms.sdk.core.execution.SdkNodeExecutionService;
import io.harness.pms.sdk.core.registries.StepRegistry;
import io.harness.pms.sdk.core.steps.io.StepResponseNotifyData;
import io.harness.pms.sdk.core.supporter.children.TestChildrenStep;
import io.harness.pms.sdk.core.supporter.children.TestChildrenStepParameters;
import io.harness.rule.Owner;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ChildrenStrategyTest extends PmsSdkCoreTestBase {
  @Mock private SdkNodeExecutionService sdkNodeExecutionService;
  @Inject @InjectMocks private ChildrenStrategy childrenStrategy;

  @Inject private StepRegistry stepRegistry;

  @Before
  public void setup() {
    stepRegistry.register(TestChildrenStep.STEP_TYPE, new TestChildrenStep());
  }

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestStart() {
    String childNodeId = generateUuid();
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .setPlanExecutionId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TestChildrenStep.STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    InvokerPackage invokerPackage =
        InvokerPackage.builder()
            .ambiance(ambiance)
            .executionMode(ExecutionMode.CHILDREN)
            .passThroughData(null)
            .stepParameters(TestChildrenStepParameters.builder().parallelNodeId(childNodeId).build())
            .build();

    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SpawnChildrenRequest> spawnChildrenRequestArgumentCaptor =
        ArgumentCaptor.forClass(SpawnChildrenRequest.class);

    childrenStrategy.start(invokerPackage);
    Mockito.verify(sdkNodeExecutionService, Mockito.times(1))
        .spawnChildren(planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(),
            spawnChildrenRequestArgumentCaptor.capture());
    assertThat(planExecutionIdCaptor.getValue()).isEqualTo(ambiance.getPlanExecutionId());
    assertThat(nodeExecutionIdCaptor.getValue()).isEqualTo(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    SpawnChildrenRequest spawnChildrenRequest = spawnChildrenRequestArgumentCaptor.getValue();

    ChildrenExecutableResponse children = spawnChildrenRequest.getChildren();
    assertThat(children.getChildrenCount()).isEqualTo(1);
    Child child = children.getChildrenList().get(0);
    assertThat(child.getChildNodeId()).isEqualTo(childNodeId);
  }

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestResume() {
    String childNodeId = generateUuid();
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TestChildrenStep.STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    ResumePackage resumePackage =
        ResumePackage.builder()
            .ambiance(ambiance)
            .stepParameters(TestChildrenStepParameters.builder().parallelNodeId(childNodeId).build())
            .responseDataMap(ImmutableMap.of(generateUuid(),
                StepResponseNotifyData.builder().nodeUuid(childNodeId).status(Status.SUCCEEDED).build()))
            .build();

    childrenStrategy.resume(resumePackage);
    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<StepResponseProto> stepResponseCaptor = ArgumentCaptor.forClass(StepResponseProto.class);

    Mockito.verify(sdkNodeExecutionService, Mockito.times(1))
        .handleStepResponse(
            planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(), stepResponseCaptor.capture());
    assertThat(nodeExecutionIdCaptor.getValue()).isEqualTo(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    assertThat(planExecutionIdCaptor.getValue()).isEqualTo(ambiance.getPlanExecutionId());

    StepResponseProto stepResponseProto = stepResponseCaptor.getValue();
    assertThat(stepResponseProto.getStatus()).isEqualTo(Status.SUCCEEDED);
  }
  private Map<String, String> setupAbstractions() {
    return ImmutableMap.<String, String>builder()
        .put(SetupAbstractionKeys.accountId, generateUuid())
        .put(SetupAbstractionKeys.orgIdentifier, generateUuid())
        .put(SetupAbstractionKeys.projectIdentifier, generateUuid())
        .build();
  }
}
