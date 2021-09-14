/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.engine.interrupts.statusupdate;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.pms.contracts.execution.Status.RUNNING;

import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.observers.NodeStatusUpdateHandler;
import io.harness.engine.observers.NodeUpdateInfo;
import io.harness.execution.NodeExecution;
import io.harness.pms.contracts.execution.Status;

import com.google.inject.Inject;
import java.util.EnumSet;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(PIPELINE)
@Slf4j
public class AbortAndRunningStepStatusUpdate implements NodeStatusUpdateHandler {
  @Inject private NodeExecutionService nodeExecutionService;

  @Override
  public void handleNodeStatusUpdate(NodeUpdateInfo nodeStatusUpdateInfo) {
    NodeExecution nodeExecution = nodeStatusUpdateInfo.getNodeExecution();
    if (nodeExecution.getParentId() == null) {
      return;
    }
    NodeExecution updatedParent = nodeExecutionService.updateStatusWithOps(
        nodeExecution.getParentId(), RUNNING, null, EnumSet.noneOf(Status.class));
    if (updatedParent == null) {
      log.warn("Cannot mark parent node to running parentId: {}, nodeExecutionId: {}", nodeExecution.getParentId(),
          nodeExecution.getUuid());
    }
  }
}
