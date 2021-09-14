/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.manifest.yaml;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.manifest.ManifestType;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfig;
import io.harness.pms.yaml.ParameterField;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;

@Value
@Builder
@OwnedBy(CDP)
@JsonTypeName(ManifestType.Kustomize)
@TypeAlias("kustomizeManifestOutcome")
@FieldNameConstants(innerTypeName = "KustomizeManifestOutcomeKeys")
@RecasterAlias("io.harness.cdng.manifest.yaml.KustomizeManifestOutcome")
public class KustomizeManifestOutcome implements ManifestOutcome {
  String identifier;
  String type = ManifestType.Kustomize;
  StoreConfig store;
  ParameterField<String> pluginPath;
  ParameterField<Boolean> skipResourceVersioning;
}
