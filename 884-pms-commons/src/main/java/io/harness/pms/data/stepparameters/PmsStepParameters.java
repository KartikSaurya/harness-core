/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.data.stepparameters;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.data.OrchestrationMap;
import io.harness.pms.serializer.recaster.RecastOrchestrationUtils;

import java.util.Map;

@OwnedBy(HarnessTeam.PIPELINE)
public class PmsStepParameters extends OrchestrationMap {
  public PmsStepParameters(Map<String, Object> map) {
    super(map);
  }

  public static PmsStepParameters parse(String json) {
    if (json == null) {
      return null;
    }
    return new PmsStepParameters(RecastOrchestrationUtils.fromJson(json));
  }

  public static PmsStepParameters parse(Map<String, Object> map) {
    if (map == null) {
      return null;
    }

    return new PmsStepParameters(map);
  }
}
