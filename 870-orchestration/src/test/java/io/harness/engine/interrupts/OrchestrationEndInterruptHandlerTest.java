/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.engine.interrupts;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.PRASHANT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.OrchestrationTestBase;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.rule.Owner;

import com.google.inject.Inject;
import java.util.concurrent.ExecutorService;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@OwnedBy(HarnessTeam.PIPELINE)
public class OrchestrationEndInterruptHandlerTest extends OrchestrationTestBase {
  @Mock private InterruptService interruptService;
  @Inject @InjectMocks private OrchestrationEndInterruptHandler orchestrationEndInterruptHandler;

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestOnEnd() {
    String planExecutionId = generateUuid();
    when(interruptService.closeActiveInterrupts(eq(planExecutionId))).thenReturn(0L);
    orchestrationEndInterruptHandler.onEnd(Ambiance.newBuilder().setPlanExecutionId(planExecutionId).build());
    ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
    verify(interruptService).closeActiveInterrupts(pidCaptor.capture());
    assertThat(pidCaptor.getValue()).isEqualTo(planExecutionId);
  }

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestGetExecutorService() {
    assertThat(orchestrationEndInterruptHandler.getInformExecutorService()).isInstanceOf(ExecutorService.class);
  }
}
