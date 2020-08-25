package io.harness.cvng.verificationjob.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.harness.cvng.core.utils.ErrorMessageUtils.generateErrorMessageFromParam;

import io.harness.cvng.core.beans.TimeRange;
import io.harness.cvng.verificationjob.beans.CanaryVerificationJobDTO;
import io.harness.cvng.verificationjob.beans.Sensitivity;
import io.harness.cvng.verificationjob.beans.VerificationJobDTO;
import io.harness.cvng.verificationjob.beans.VerificationJobType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@FieldNameConstants(innerTypeName = "CanaryVerificationJobKeys")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CanaryVerificationJob extends VerificationJob {
  public static final Duration PRE_DEPLOYMENT_DATA_COLLECTION_DURATION = Duration.ofMinutes(15);
  private Sensitivity sensitivity;
  private Integer trafficSplitPercentage;
  @Override
  public VerificationJobType getType() {
    return VerificationJobType.CANARY;
  }

  @Override
  public VerificationJobDTO getVerificationJobDTO() {
    CanaryVerificationJobDTO canaryVerificationJobDTO = new CanaryVerificationJobDTO();
    populateCommonFields(canaryVerificationJobDTO);
    return canaryVerificationJobDTO;
  }

  @Override
  protected void validateParams() {
    checkNotNull(sensitivity, generateErrorMessageFromParam(CanaryVerificationJobKeys.sensitivity));
    Optional.ofNullable(trafficSplitPercentage)
        .ifPresent(percentage
            -> checkState(percentage >= 0 && percentage <= 100,
                CanaryVerificationJobKeys.trafficSplitPercentage + " is not in appropriate range"));
  }

  @Override
  public List<TimeRange> getDataCollectionTimeRanges(Instant startTime) {
    List<TimeRange> timeRanges = new ArrayList<>();
    timeRanges.add(TimeRange.builder()
                       .startTime(startTime.minus(PRE_DEPLOYMENT_DATA_COLLECTION_DURATION))
                       .endTime(startTime)
                       .build());
    timeRanges.addAll(getTimeRangesForDuration(startTime));
    return timeRanges;
  }
}
