/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.sdk.core.execution.invokers;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.pms.sdk.core.supporter.async.TestTaskStep.TASK_STEP_TYPE;
import static io.harness.rule.OwnerRule.SAHIL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.ExecutionMode;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.events.QueueTaskRequest;
import io.harness.pms.contracts.steps.io.StepResponseProto;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.plan.execution.SetupAbstractionKeys;
import io.harness.pms.sdk.core.PmsSdkCoreTestBase;
import io.harness.pms.sdk.core.execution.InvokerPackage;
import io.harness.pms.sdk.core.execution.ResumePackage;
import io.harness.pms.sdk.core.execution.SdkNodeExecutionService;
import io.harness.pms.sdk.core.registries.StepRegistry;
import io.harness.pms.sdk.core.supporter.async.TestStepParameters;
import io.harness.pms.sdk.core.supporter.async.TestTaskStep;
import io.harness.pms.sdk.core.waiter.AsyncWaitEngine;
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
public class TaskStrategyTest extends PmsSdkCoreTestBase {
  @Mock private SdkNodeExecutionService sdkNodeExecutionService;
  @Mock private AsyncWaitEngine asyncWaitEngine;
  @Inject @InjectMocks private TaskStrategy taskStrategy;

  @Inject private StepRegistry stepRegistry;

  @Before
  public void setup() {
    stepRegistry.register(TASK_STEP_TYPE, new TestTaskStep());
  }

  @Test
  @Owner(developers = SAHIL)
  @Category(UnitTests.class)
  public void shouldTestStart() {
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TASK_STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    InvokerPackage invokerPackage = InvokerPackage.builder()
                                        .ambiance(ambiance)
                                        .executionMode(ExecutionMode.TASK)
                                        .passThroughData(null)
                                        .stepParameters(TestStepParameters.builder().param("TEST_PARAM").build())
                                        .build();
    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<QueueTaskRequest> spawnChildRequest = ArgumentCaptor.forClass(QueueTaskRequest.class);

    taskStrategy.start(invokerPackage);
    verify(sdkNodeExecutionService)
        .queueTaskRequest(
            planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(), spawnChildRequest.capture());

    QueueTaskRequest request = spawnChildRequest.getValue();
    assertThat(request.getStatus()).isEqualTo(Status.TASK_WAITING);
  }

  @Test
  @Owner(developers = SAHIL)
  @Category(UnitTests.class)
  public void shouldTestResume() {
    Ambiance ambiance = Ambiance.newBuilder()
                            .putAllSetupAbstractions(setupAbstractions())
                            .setPlanId(generateUuid())
                            .setPlanExecutionId(generateUuid())
                            .addLevels(Level.newBuilder()
                                           .setSetupId(generateUuid())
                                           .setRuntimeId(generateUuid())
                                           .setStepType(TASK_STEP_TYPE)
                                           .setIdentifier(generateUuid())
                                           .build())
                            .build();
    ResumePackage resumePackage = ResumePackage.builder()
                                      .ambiance(ambiance)
                                      .stepParameters(TestStepParameters.builder().param("TEST_PARAM").build())
                                      .responseDataMap(ImmutableMap.of(generateUuid(),
                                          StringNotifyResponseData.builder().data("someString").build()))
                                      .build();

    ArgumentCaptor<String> planExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> nodeExecutionIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<StepResponseProto> stepResponseCaptor = ArgumentCaptor.forClass(StepResponseProto.class);
    taskStrategy.resume(resumePackage);
    Mockito.verify(sdkNodeExecutionService, Mockito.times(1))
        .handleStepResponse(
            planExecutionIdCaptor.capture(), nodeExecutionIdCaptor.capture(), stepResponseCaptor.capture());

    assertThat(nodeExecutionIdCaptor.getValue()).isEqualTo(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    assertThat(planExecutionIdCaptor.getValue()).isEqualTo(ambiance.getPlanExecutionId());
  }

  private Map<String, String> setupAbstractions() {
    return ImmutableMap.<String, String>builder()
        .put(SetupAbstractionKeys.accountId, generateUuid())
        .put(SetupAbstractionKeys.orgIdentifier, generateUuid())
        .put(SetupAbstractionKeys.projectIdentifier, generateUuid())
        .build();
  }
}
