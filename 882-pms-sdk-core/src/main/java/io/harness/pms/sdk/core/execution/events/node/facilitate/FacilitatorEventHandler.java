package io.harness.pms.sdk.core.execution.events.node.facilitate;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.facilitators.FacilitatorEvent;
import io.harness.pms.contracts.facilitators.FacilitatorObtainment;
import io.harness.pms.contracts.facilitators.FacilitatorResponseProto;
import io.harness.pms.contracts.plan.NodeExecutionEventType;
import io.harness.pms.contracts.refobjects.RefObject;
import io.harness.pms.events.base.PmsBaseEventHandler;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.sdk.core.execution.EngineObtainmentHelper;
import io.harness.pms.sdk.core.execution.SdkNodeExecutionService;
import io.harness.pms.sdk.core.registries.FacilitatorRegistry;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepParameters;
import io.harness.pms.serializer.recaster.RecastOrchestrationUtils;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@OwnedBy(HarnessTeam.PIPELINE)
public class FacilitatorEventHandler extends PmsBaseEventHandler<FacilitatorEvent> {
  @Inject private FacilitatorRegistry facilitatorRegistry;
  @Inject private SdkNodeExecutionService sdkNodeExecutionService;
  @Inject private EngineObtainmentHelper engineObtainmentHelper;

  @Override
  protected Map<String, String> extractMetricContext(FacilitatorEvent message) {
    return ImmutableMap.<String, String>builder()
        .put("accountId", AmbianceUtils.getAccountId(message.getAmbiance()))
        .put("projectIdentifier", AmbianceUtils.getOrgIdentifier(message.getAmbiance()))
        .put("orgIdentifier", AmbianceUtils.getProjectIdentifier(message.getAmbiance()))
        .build();
  }

  @Override
  protected String getMetricPrefix(FacilitatorEvent message) {
    return "facilitator_event";
  }

  @Override
  protected Map<String, String> extraLogProperties(FacilitatorEvent event) {
    return ImmutableMap.<String, String>builder()
        .put("eventType", NodeExecutionEventType.FACILITATE.name())
        .put("notifyId", event.getNotifyId())
        .build();
  }

  @Override
  protected Ambiance extractAmbiance(FacilitatorEvent event) {
    return event.getAmbiance();
  }

  @Override
  protected boolean handleEventWithContext(FacilitatorEvent event) {
    try {
      log.info("Starting to handle FACILITATION event");
      Ambiance ambiance = event.getAmbiance();
      StepInputPackage inputPackage = obtainInputPackage(ambiance, event.getRefObjectsList());
      FacilitatorResponse currFacilitatorResponse = null;
      for (FacilitatorObtainment obtainment : event.getFacilitatorObtainmentsList()) {
        Facilitator facilitator = facilitatorRegistry.obtain(obtainment.getType());
        StepParameters stepParameters =
            RecastOrchestrationUtils.fromDocumentJson(event.getStepParameters().toStringUtf8(), StepParameters.class);
        currFacilitatorResponse =
            facilitator.facilitate(ambiance, stepParameters, obtainment.getParameters().toByteArray(), inputPackage);
        if (currFacilitatorResponse != null) {
          break;
        }
      }
      if (currFacilitatorResponse == null) {
        log.info("Calculated Facilitator response is null. Returning response Successful false");
        sdkNodeExecutionService.handleFacilitationResponse(event.getNodeExecutionId(), event.getNotifyId(),
            FacilitatorResponseProto.newBuilder().setIsSuccessful(false).build());
        return true;
      }
      sdkNodeExecutionService.handleFacilitationResponse(event.getNodeExecutionId(), event.getNotifyId(),
          FacilitatorResponseMapper.toFacilitatorResponseProto(currFacilitatorResponse));
      log.info("Facilitation Event Handled Successfully");
      return true;
    } catch (Exception ex) {
      log.error("Error while facilitating execution", ex);
      return false;
    }
  }

  private StepInputPackage obtainInputPackage(Ambiance ambiance, List<RefObject> refObjectList) {
    return engineObtainmentHelper.obtainInputPackage(ambiance, refObjectList);
  }
}
