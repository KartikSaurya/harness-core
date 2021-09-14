/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.steps.resourcerestraint;

import static io.harness.pms.utils.PmsConstants.QUEUING_RC_NAME;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.distribution.constraint.Constraint.Strategy;
import io.harness.engine.observers.OrchestrationStartObserver;
import io.harness.engine.observers.beans.OrchestrationStartInfo;
import io.harness.exception.InvalidRequestException;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.steps.resourcerestraint.beans.ResourceRestraint;
import io.harness.steps.resourcerestraint.service.ResourceRestraintService;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.PIPELINE)
@Slf4j
public class ResourceRestraintInitializer implements OrchestrationStartObserver {
  @Inject private ResourceRestraintService resourceRestraintService;

  @Override
  public void onStart(OrchestrationStartInfo orchestrationStartInfo) {
    final String accountId = AmbianceUtils.getAccountId(orchestrationStartInfo.getAmbiance());
    try {
      ResourceRestraint resourceConstraint = ResourceRestraint.builder()
                                                 .name(QUEUING_RC_NAME)
                                                 .accountId(accountId)
                                                 .capacity(1)
                                                 .harnessOwned(true)
                                                 .strategy(Strategy.FIFO)
                                                 .build();
      resourceRestraintService.save(resourceConstraint);
    } catch (InvalidRequestException e) {
      log.info("Resource Constraint already exist for name {}", QUEUING_RC_NAME);
    }
  }
}
