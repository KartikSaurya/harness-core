package io.harness.state.core.section;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.ambiance.Ambiance;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.ResponseData;
import io.harness.facilitator.modes.child.ChildExecutable;
import io.harness.facilitator.modes.child.ChildExecutableResponse;
import io.harness.state.Step;
import io.harness.state.StepType;
import io.harness.state.io.StepInputPackage;
import io.harness.state.io.StepResponse;
import io.harness.state.io.StepResponse.StepResponseBuilder;
import io.harness.state.io.StepResponseNotifyData;

import java.util.Map;

@OwnedBy(CDC)
public class SectionStep implements Step, ChildExecutable<SectionStepParameters> {
  public static final StepType STEP_TYPE = StepType.builder().type("SECTION").build();

  @Override
  public ChildExecutableResponse obtainChild(
      Ambiance ambiance, SectionStepParameters stepParameters, StepInputPackage inputPackage) {
    return ChildExecutableResponse.builder().childNodeId(stepParameters.getChildNodeId()).build();
  }

  @Override
  public StepResponse handleChildResponse(
      Ambiance ambiance, SectionStepParameters stepParameters, Map<String, ResponseData> responseDataMap) {
    StepResponseBuilder responseBuilder = StepResponse.builder();
    StepResponseNotifyData stepResponseNotifyData = (StepResponseNotifyData) responseDataMap.values().iterator().next();
    responseBuilder.status(stepResponseNotifyData.getStatus());
    return responseBuilder.build();
  }
}
