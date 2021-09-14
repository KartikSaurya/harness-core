/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.engine.interrupts.handlers;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.interrupts.Interrupt.State.PROCESSED_UNSUCCESSFULLY;

import io.harness.OrchestrationPublisherName;
import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.executions.node.NodeExecutionUpdateFailedException;
import io.harness.engine.interrupts.InterruptProcessingFailedException;
import io.harness.engine.interrupts.callback.FailureInterruptCallback;
import io.harness.engine.interrupts.handlers.publisher.InterruptEventPublisher;
import io.harness.exception.InvalidRequestException;
import io.harness.interrupts.Interrupt;
import io.harness.pms.contracts.interrupts.InterruptType;
import io.harness.waiter.WaitNotifyEngine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(PIPELINE)
@Slf4j
// TODO : This is not a MarkStatus interrupt handler make it extend interrupt handler
public class CustomFailureInterruptHandler extends MarkStatusInterruptHandler {
  @Inject private InterruptEventPublisher interruptEventPublisher;
  @Inject private WaitNotifyEngine waitNotifyEngine;
  @Inject @Named(OrchestrationPublisherName.PUBLISHER_NAME) String publisherName;

  @Override
  public Interrupt handleInterruptForNodeExecution(Interrupt interrupt, String nodeExecutionId) {
    try {
      String notifyId = interruptEventPublisher.publishEvent(nodeExecutionId, interrupt, InterruptType.CUSTOM_FAILURE);
      waitNotifyEngine.waitForAllOn(publisherName,
          FailureInterruptCallback.builder()
              .nodeExecutionId(nodeExecutionId)
              .interruptId(interrupt.getUuid())
              .interruptType(interrupt.getType())
              .interruptConfig(interrupt.getInterruptConfig())
              .build(),
          notifyId);
      return interruptService.markProcessing(interrupt.getUuid());
    } catch (NodeExecutionUpdateFailedException ex) {
      interruptService.markProcessed(interrupt.getUuid(), PROCESSED_UNSUCCESSFULLY);
      throw new InterruptProcessingFailedException(InterruptType.CUSTOM_FAILURE,
          "Custom Failure Interrupt failed for execution Plan :" + interrupt.getPlanExecutionId()
              + "for NodeExecutionId: " + nodeExecutionId,
          ex);
    } catch (Exception e) {
      interruptService.markProcessed(interrupt.getUuid(), PROCESSED_UNSUCCESSFULLY);
      log.error("Error in handling Custom Failure Interrupt", e);
      throw new InvalidRequestException("Error in handling Custom Failure Interrupt, " + e.getMessage());
    }
  }
}
