package io.harness.engine.pms.start;

import static io.harness.pms.events.PmsEventFrameworkConstants.PIPELINE_MONITORING_ENABLED;
import static io.harness.pms.events.PmsEventFrameworkConstants.SERVICE_NAME;
import static io.harness.springdata.SpringDataMongoUtils.setUnset;

import static java.lang.String.format;

import io.harness.beans.FeatureName;
import io.harness.data.structure.HarnessStringUtils;
import io.harness.engine.ExecutionCheck;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.executions.node.NodeExecutionTimeoutCallback;
import io.harness.engine.interrupts.InterruptService;
import io.harness.engine.utils.OrchestrationEventsFrameworkUtils;
import io.harness.eventsframework.api.Producer;
import io.harness.eventsframework.producer.Message;
import io.harness.execution.ExecutionModeUtils;
import io.harness.execution.NodeExecution;
import io.harness.execution.NodeExecution.NodeExecutionKeys;
import io.harness.pms.PmsFeatureFlagService;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.start.NodeStartEvent;
import io.harness.pms.contracts.facilitators.FacilitatorResponseProto;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.registries.timeout.TimeoutRegistry;
import io.harness.serializer.KryoSerializer;
import io.harness.timeout.TimeoutCallback;
import io.harness.timeout.TimeoutEngine;
import io.harness.timeout.TimeoutInstance;
import io.harness.timeout.TimeoutParameters;
import io.harness.timeout.TimeoutTracker;
import io.harness.timeout.TimeoutTrackerFactory;
import io.harness.timeout.contracts.TimeoutObtainment;
import io.harness.timeout.trackers.absolute.AbsoluteTimeoutParameters;
import io.harness.timeout.trackers.absolute.AbsoluteTimeoutTrackerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeStartHelper {
  @Inject private OrchestrationEventsFrameworkUtils eventsFrameworkUtils;
  @Inject private InterruptService interruptService;
  @Inject private NodeExecutionService nodeExecutionService;
  @Inject private KryoSerializer kryoSerializer;
  @Inject private TimeoutEngine timeoutEngine;
  @Inject private TimeoutRegistry timeoutRegistry;
  @Inject private PmsFeatureFlagService pmsFeatureFlagService;

  public void startNode(Ambiance ambiance, FacilitatorResponseProto facilitatorResponse) {
    ExecutionCheck check = interruptService.checkInterruptsPreInvocation(
        ambiance.getPlanExecutionId(), AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    if (!check.isProceed()) {
      log.info("Not Proceeding with Execution : {}", check.getReason());
      return;
    }
    NodeExecution nodeExecution = prepareNodeExecutionForInvocation(ambiance);
    log.info("Sending NodeExecution START event");
    sendEvent(nodeExecution, facilitatorResponse.getPassThroughDataBytes());
  }

  private void sendEvent(NodeExecution nodeExecution, ByteString passThroughData) {
    NodeStartEvent nodeStartEvent = NodeStartEvent.newBuilder()
                                        .setAmbiance(nodeExecution.getAmbiance())
                                        .addAllRefObjects(nodeExecution.getNode().getRebObjectsList())
                                        .setFacilitatorPassThoroughData(passThroughData)
                                        .setStepParameters(ByteString.copyFromUtf8(HarnessStringUtils.emptyIfNull(
                                            nodeExecution.getResolvedStepParameters().toJson())))
                                        .setMode(nodeExecution.getMode())
                                        .build();
    Producer producer = eventsFrameworkUtils.obtainProducerForNodeStart(nodeExecution.getNode().getServiceName());
    Map<String, String> metadataMap = new HashMap<>();
    metadataMap.put(SERVICE_NAME, nodeExecution.getNode().getServiceName());
    if (pmsFeatureFlagService.isEnabled(
            AmbianceUtils.getAccountId(nodeStartEvent.getAmbiance()), FeatureName.PIPELINE_MONITORING)) {
      metadataMap.put(PIPELINE_MONITORING_ENABLED, "true");
    }
    producer.send(Message.newBuilder().putAllMetadata(metadataMap).setData(nodeStartEvent.toByteString()).build());
    log.info("Successfully Sent NodeStart event to the producer");
  }

  private NodeExecution prepareNodeExecutionForInvocation(Ambiance ambiance) {
    NodeExecution nodeExecution = nodeExecutionService.get(AmbianceUtils.obtainCurrentRuntimeId(ambiance));
    return Preconditions.checkNotNull(nodeExecutionService.updateStatusWithOps(
        AmbianceUtils.obtainCurrentRuntimeId(ambiance), Status.RUNNING, ops -> {
          ops.set(NodeExecutionKeys.startTs, System.currentTimeMillis());
          if (!ExecutionModeUtils.isParentMode(nodeExecution.getMode())) {
            setUnset(ops, NodeExecutionKeys.timeoutInstanceIds, registerTimeouts(nodeExecution));
          }
        }, EnumSet.noneOf(Status.class)));
  }

  private List<String> registerTimeouts(NodeExecution nodeExecution) {
    List<TimeoutObtainment> timeoutObtainmentList;
    if (nodeExecution.getNode().getTimeoutObtainmentsList().isEmpty()) {
      timeoutObtainmentList = Collections.singletonList(
          TimeoutObtainment.newBuilder()
              .setDimension(AbsoluteTimeoutTrackerFactory.DIMENSION)
              .setParameters(ByteString.copyFrom(
                  kryoSerializer.asBytes(AbsoluteTimeoutParameters.builder()
                                             .timeoutMillis(TimeoutParameters.DEFAULT_TIMEOUT_IN_MILLIS)
                                             .build())))
              .build());
    } else {
      timeoutObtainmentList = nodeExecution.getNode().getTimeoutObtainmentsList();
    }

    List<String> timeoutInstanceIds = new ArrayList<>();
    TimeoutCallback timeoutCallback =
        new NodeExecutionTimeoutCallback(nodeExecution.getAmbiance().getPlanExecutionId(), nodeExecution.getUuid());
    for (TimeoutObtainment timeoutObtainment : timeoutObtainmentList) {
      TimeoutTrackerFactory timeoutTrackerFactory = timeoutRegistry.obtain(timeoutObtainment.getDimension());
      TimeoutTracker timeoutTracker = timeoutTrackerFactory.create(
          (TimeoutParameters) kryoSerializer.asObject(timeoutObtainment.getParameters().toByteArray()));
      TimeoutInstance instance = timeoutEngine.registerTimeout(timeoutTracker, timeoutCallback);
      timeoutInstanceIds.add(instance.getUuid());
    }
    log.info(format("Registered node execution timeouts: %s", timeoutInstanceIds.toString()));
    return timeoutInstanceIds;
  }
}
