/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OwnedBy(CDP)
public abstract class YamlWithComputeProvider extends InfraMappingYaml {
  private String computeProviderType;
  private String computeProviderName;

  public YamlWithComputeProvider(String type, String harnessApiVersion, String serviceName, String infraMappingType,
      String deploymentType, String computeProviderType, String computeProviderName, Map<String, Object> blueprints) {
    super(type, harnessApiVersion, serviceName, infraMappingType, deploymentType, blueprints);
    this.computeProviderType = computeProviderType;
    this.computeProviderName = computeProviderName;
  }
}
