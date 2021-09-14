/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cvng.beans.splunk;

import static io.harness.annotations.dev.HarnessTeam.CV;

import io.harness.annotations.dev.OwnedBy;
import io.harness.cvng.beans.DataCollectionRequestType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeName("SPLUNK_LATEST_HISTOGRAM")
@SuperBuilder
@NoArgsConstructor
@FieldNameConstants(innerTypeName = "SplunkLatestHistogramDataCollectionRequestKeys")
@OwnedBy(CV)
public class SplunkLatestHistogramDataCollectionRequest extends SplunkDataCollectionRequest {
  private static int HISTOGRAM_SIZE_IN_DAYS = 7;

  private String query;

  @Override
  public String getDSL() {
    return SplunkLatestHistogramDataCollectionRequest.readDSL(
        "splunk-histogram.datacollection", SplunkLatestHistogramDataCollectionRequest.class);
  }

  @Override
  public DataCollectionRequestType getType() {
    return DataCollectionRequestType.SPLUNK_LATEST_HISTOGRAM;
  }

  @Override
  public Instant getEndTime(Instant currentTime) {
    return currentTime;
  }

  @Override
  public Instant getStartTime(Instant currentTime) {
    return currentTime.minus(Duration.ofDays(HISTOGRAM_SIZE_IN_DAYS));
  }

  @Override
  public Map<String, Object> fetchDslEnvVariables() {
    return new HashMap<String, Object>() {
      { put(SplunkLatestHistogramDataCollectionRequestKeys.query, query); }
    };
  }
}
