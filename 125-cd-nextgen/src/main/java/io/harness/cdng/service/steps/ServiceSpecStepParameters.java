/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.service.steps;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.artifact.bean.yaml.ArtifactOverrideSets.ArtifactOverrideSetsStepParametersWrapper;
import io.harness.cdng.manifest.yaml.ManifestOverrideSets.ManifestOverrideSetsStepParametersWrapper;
import io.harness.cdng.variables.beans.NGVariableOverrideSetWrapper;
import io.harness.pms.sdk.core.steps.io.StepParameters;
import io.harness.pms.yaml.ParameterField;
import io.harness.pms.yaml.SkipAutoEvaluation;
import io.harness.yaml.core.variables.NGVariable;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(HarnessTeam.CDC)
@Value
@Builder
@TypeAlias("serviceSpecStepParameters")
@RecasterAlias("io.harness.cdng.service.steps.ServiceSpecStepParameters")
public class ServiceSpecStepParameters implements StepParameters {
  @SkipAutoEvaluation ParameterField<List<NGVariable>> originalVariables;
  @SkipAutoEvaluation ParameterField<List<NGVariableOverrideSetWrapper>> originalVariableOverrideSets;
  @SkipAutoEvaluation ParameterField<List<NGVariable>> stageOverrideVariables;
  ParameterField<List<String>> stageOverridesUseVariableOverrideSets;

  Map<String, ArtifactOverrideSetsStepParametersWrapper> artifactOverrideSets;
  Map<String, ManifestOverrideSetsStepParametersWrapper> manifestOverrideSets;
  List<String> childrenNodeIds;
}
