/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.sdk.core.execution.invokers;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.SAHIL;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.ChildChainExecutableResponse;
import io.harness.pms.contracts.execution.ExecutionMode;
import io.harness.pms.contracts.execution.events.SpawnChildRequest;
import io.harness.pms.contracts.execution.events.SuspendChainRequest;
import io.harness.pms.contracts.steps.io.StepResponseProto;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.plan.execution.SetupAbstractionKeys;
import io.harness.pms.sdk.core.PmsSdkCoreTestBase;
import io.harness.pms.sdk.core.execution.ChainDetails;
import io.harness.pms.sdk.core.execution.InvokerPackage;
import io.harness.pms.sdk.core.execution.ResumePackage;
import io.harness.pms.sdk.core.execution.SdkNodeExecutionService;
import io.harness.pms.sdk.core.registries.StepRegistry;
import io.harness.pms.sdk.core.supporter.children.TestChildChainStep;
import io.harness.pms.sdk.core.supporter.children.TestChildrenStepParameters;
import io.harness.rule.Owner;
import io.harness.waiter.StringNotifyResponseData;

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

@OwnedBy(HarnessTeam.PIPELINE)
public class ChildChainStrategyTest extends PmsSdkCoreTestBase {
  @Mock private SdkNodeExecutionService sdkNodeExecutionService;
  @Inject @InjectMocks private ChildChainStrategy childrenStrategy;

  @Inject private StepRegistry stepRegistry;

  @Before
  public void setup() {
    stepRegistry.register(TestChildChainStep.STEP_TYPE, new TestChildChainStep());
  }

  @Test
  @Owner(developers = SAHIL)
  @Category(UnitTests.class)
  public void start() {
    String childNodeId = generateUuid();
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .setPlanExecutionId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TestChildChainStep.STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    InvokerPackage invokerPackage =
        InvokerPackage.builder()
            .ambiance(ambiance)
            .executionMode(ExecutionMode.CHILD_CHAIN)
            .passThroughData(null)
            .stepParameters(TestChildrenStepParameters.builder().parallelNodeId(childNodeId).build())
            .build();

    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SpawnChildRequest> spawnChildrenRequestArgumentCaptor =
        ArgumentCaptor.forClass(SpawnChildRequest.class);

    childrenStrategy.start(invokerPackage);
    Mockito.verify(sdkNodeExecutionService, Mockito.times(1))
        .spawnChild(planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(),
            spawnChildrenRequestArgumentCaptor.capture());
    assertThat(planExecutionIdCaptor.getValue()).isEqualTo(ambiance.getPlanExecutionId());
    assertThat(nodeExecutionIdCaptor.getValue()).isEqualTo(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    SpawnChildRequest spawnChildrenRequest = spawnChildrenRequestArgumentCaptor.getValue();

    ChildChainExecutableResponse children = spawnChildrenRequest.getChildChain();
    assertThat(children.getNextChildId()).isEqualTo("test");
  }

  @Test
  @Owner(developers = SAHIL)
  @Category(UnitTests.class)
  public void testResumeWithChildChainEnd() {
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .setPlanExecutionId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TestChildChainStep.STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    ResumePackage resumePackage = ResumePackage.builder()
                                      .ambiance(ambiance)
                                      .stepParameters(TestChildrenStepParameters.builder().build())
                                      .responseDataMap(ImmutableMap.of(generateUuid(),
                                          StringNotifyResponseData.builder().data("someString").build()))
                                      .chainDetails(ChainDetails.builder().shouldEnd(true).build())
                                      .build();

    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<StepResponseProto> stepResponseCaptor = ArgumentCaptor.forClass(StepResponseProto.class);
    childrenStrategy.resume(resumePackage);
    Mockito.verify(sdkNodeExecutionService, Mockito.times(1))
        .handleStepResponse(
            planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(), stepResponseCaptor.capture());

    assertThat(nodeExecutionIdCaptor.getValue()).isEqualTo(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    assertThat(planExecutionIdCaptor.getValue()).isEqualTo(ambiance.getPlanExecutionId());
  }

  @Test
  @Owner(developers = SAHIL)
  @Category(UnitTests.class)
  public void testResume() {
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .setPlanExecutionId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TestChildChainStep.STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    ResumePackage resumePackage = ResumePackage.builder()
                                      .ambiance(ambiance)
                                      .stepParameters(TestChildrenStepParameters.builder().build())
                                      .responseDataMap(ImmutableMap.of(generateUuid(),
                                          StringNotifyResponseData.builder().data("someString").build()))
                                      .chainDetails(ChainDetails.builder().shouldEnd(false).build())
                                      .build();

    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SuspendChainRequest> suspendChainRequest = ArgumentCaptor.forClass(SuspendChainRequest.class);
    childrenStrategy.resume(resumePackage);
    Mockito.verify(sdkNodeExecutionService, Mockito.times(1))
        .suspendChainExecution(
            planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(), suspendChainRequest.capture());

    assertThat(nodeExecutionIdCaptor.getValue()).isEqualTo(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    assertThat(planExecutionIdCaptor.getValue()).isEqualTo(ambiance.getPlanExecutionId());
    assertThat(suspendChainRequest.getValue().getExecutableResponse().getChildChain().getSuspend()).isEqualTo(true);
  }

  private Map<String, String> setupAbstractions() {
    return ImmutableMap.<String, String>builder()
        .put(SetupAbstractionKeys.accountId, generateUuid())
        .put(SetupAbstractionKeys.orgIdentifier, generateUuid())
        .put(SetupAbstractionKeys.projectIdentifier, generateUuid())
        .build();
  }
}
