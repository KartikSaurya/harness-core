package io.harness.state.io;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

import io.harness.annotations.Redesign;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.Outcome;
import io.harness.pms.execution.Status;
import io.harness.pms.execution.failure.FailureInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@OwnedBy(CDC)
@Value
@Builder(toBuilder = true)
@Redesign
public class StepResponse {
  @NonNull Status status;
  @Singular Collection<StepOutcome> stepOutcomes;
  FailureInfo failureInfo;

  @Value
  @Builder
  public static class StepOutcome {
    String group;
    @NonNull String name;
    Outcome outcome;
  }

  public Map<String, StepOutcome> stepOutcomeMap() {
    Map<String, StepOutcome> stepOutcomeMap = new HashMap<>();
    if (isEmpty(stepOutcomes)) {
      return stepOutcomeMap;
    }
    for (StepOutcome stepOutcome : stepOutcomes) {
      stepOutcomeMap.put(stepOutcome.getName(), stepOutcome);
    }
    return stepOutcomeMap;
  }
}
