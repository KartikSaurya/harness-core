/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ng.cdOverview.dto;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@OwnedBy(HarnessTeam.DX)
@Value
@Builder
@AllArgsConstructor
public class EnvIdCountPair {
  String envId;
  int count;
}
