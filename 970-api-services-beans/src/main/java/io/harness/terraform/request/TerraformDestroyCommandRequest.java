/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.terraform.request;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(HarnessTeam.CDP)
public class TerraformDestroyCommandRequest {
  List<String> targets;
  List<String> varFilePaths;
  String varParams; // Needed to send inline variable values in CG
  String uiLogs; // Needed in CG to prevent printing secrets
}
