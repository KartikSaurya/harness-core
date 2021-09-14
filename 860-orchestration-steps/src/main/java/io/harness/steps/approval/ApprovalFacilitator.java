/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.steps.approval;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.ExecutionMode;
import io.harness.pms.contracts.facilitators.FacilitatorType;
import io.harness.pms.sdk.core.execution.events.node.facilitate.Facilitator;
import io.harness.pms.sdk.core.execution.events.node.facilitate.FacilitatorResponse;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepParameters;

import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.PIPELINE)
@Slf4j
public class ApprovalFacilitator implements Facilitator {
  public static final String APPROVAL_FACILITATOR = "APPROVAL_FACILITATOR";

  public static final FacilitatorType FACILITATOR_TYPE =
      FacilitatorType.newBuilder().setType(APPROVAL_FACILITATOR).build();

  @Override
  public FacilitatorResponse facilitate(
      Ambiance ambiance, StepParameters stepParameters, byte[] parameters, StepInputPackage inputPackage) {
    return FacilitatorResponse.builder().executionMode(ExecutionMode.APPROVAL).build();
  }
}
