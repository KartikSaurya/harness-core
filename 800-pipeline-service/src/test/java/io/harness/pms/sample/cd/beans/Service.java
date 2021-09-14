/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.sample.cd.beans;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.yaml.YamlNode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@OwnedBy(HarnessTeam.PIPELINE)
@Data
@NoArgsConstructor
public class Service {
  @JsonProperty(YamlNode.UUID_FIELD_NAME) String uuid;
  String identifier;
  String name;
  ServiceDefinition serviceDefinition;
}
