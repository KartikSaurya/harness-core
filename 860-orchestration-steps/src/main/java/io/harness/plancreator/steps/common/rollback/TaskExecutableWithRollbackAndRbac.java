/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.plancreator.steps.common.rollback;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.sdk.core.resolver.outputs.ExecutionSweepingOutputService;
import io.harness.steps.executable.TaskExecutableWithRbac;
import io.harness.tasks.ResponseData;

import com.google.inject.Inject;
import java.util.Map;

@OwnedBy(CDC)
public abstract class TaskExecutableWithRollbackAndRbac<R extends ResponseData>
    implements TaskExecutableWithRbac<StepElementParameters, R> {
  @Inject ExecutionSweepingOutputService executionSweepingOutputService;

  @Override
  public void handleFailureInterrupt(
      Ambiance ambiance, StepElementParameters stepParameters, Map<String, String> metadata) {
    RollbackExecutableUtility.publishRollbackInfo(ambiance, stepParameters, metadata, executionSweepingOutputService);
  }
}
