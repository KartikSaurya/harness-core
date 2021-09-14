/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.provision.terraform;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.yaml.ParameterField;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OwnedBy(HarnessTeam.CDP)
@RecasterAlias("io.harness.cdng.provision.terraform.TerraformExecutionDataParameters")
public class TerraformExecutionDataParameters {
  ParameterField<String> workspace;
  TerraformConfigFilesWrapper configFiles;
  LinkedHashMap<String, TerraformVarFile> varFiles;
  TerraformBackendConfig backendConfig;
  ParameterField<List<String>> targets;
  Map<String, Object> environmentVariables;
}
