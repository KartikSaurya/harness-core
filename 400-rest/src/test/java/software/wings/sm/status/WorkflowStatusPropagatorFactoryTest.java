/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.sm.status;

import static io.harness.rule.OwnerRule.PRASHANT;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.beans.ExecutionStatus;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import software.wings.WingsBaseTest;
import software.wings.sm.status.handlers.NoopWorkflowPropagator;
import software.wings.sm.status.handlers.WorkflowPausePropagator;
import software.wings.sm.status.handlers.WorkflowResumePropagator;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class WorkflowStatusPropagatorFactoryTest extends WingsBaseTest {
  @Inject private WorkflowStatusPropagatorFactory propagatorFactory;

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestObtainHandler() {
    WorkflowStatusPropagator statusPropagator = propagatorFactory.obtainHandler(ExecutionStatus.PAUSED);
    assertThat(statusPropagator).isNotNull();
    assertThat(statusPropagator).isInstanceOf(WorkflowPausePropagator.class);

    statusPropagator = propagatorFactory.obtainHandler(ExecutionStatus.RESUMED);
    assertThat(statusPropagator).isNotNull();
    assertThat(statusPropagator).isInstanceOf(WorkflowResumePropagator.class);

    statusPropagator = propagatorFactory.obtainHandler(ExecutionStatus.ABORTED);
    assertThat(statusPropagator).isInstanceOf(NoopWorkflowPropagator.class);
  }
}
