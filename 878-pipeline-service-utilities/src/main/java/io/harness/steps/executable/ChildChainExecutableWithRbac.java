/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.steps.executable;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.ChildChainExecutableResponse;
import io.harness.pms.sdk.core.steps.executables.ChildChainExecutable;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepParameters;
import io.harness.pms.security.PmsSecurityContextEventGuard;

import lombok.SneakyThrows;

@OwnedBy(PIPELINE)
public interface ChildChainExecutableWithRbac<T extends StepParameters> extends ChildChainExecutable<T> {
  void validateResources(Ambiance ambiance, T stepParameters);

  @SneakyThrows
  @Override
  default ChildChainExecutableResponse executeFirstChild(
      Ambiance ambiance, T stepParameters, StepInputPackage inputPackage) {
    try (PmsSecurityContextEventGuard securityContextEventGuard = new PmsSecurityContextEventGuard(ambiance)) {
      validateResources(ambiance, stepParameters);
      return this.executeFirstChildAfterRbac(ambiance, stepParameters, inputPackage);
    }
  }

  ChildChainExecutableResponse executeFirstChildAfterRbac(
      Ambiance ambiance, T stepParameters, StepInputPackage inputPackage);
}
