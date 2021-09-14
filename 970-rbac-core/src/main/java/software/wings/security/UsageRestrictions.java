/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.security;

import io.harness.yaml.BaseYaml;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsageRestrictions {
  private Set<AppEnvRestriction> appEnvRestrictions;

  @Data
  @Builder
  @EqualsAndHashCode
  public static class AppEnvRestriction {
    private GenericEntityFilter appFilter;
    private EnvFilter envFilter;

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Yaml extends BaseYaml {
      private GenericEntityFilter.Yaml appFilter;
      private EnvFilter.Yaml envFilter;

      @Builder
      public Yaml(GenericEntityFilter.Yaml appFilter, EnvFilter.Yaml envFilter) {
        this.appFilter = appFilter;
        this.envFilter = envFilter;
      }
    }
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  public static class Yaml extends BaseYaml {
    private List<AppEnvRestriction.Yaml> appEnvRestrictions;

    @Builder
    public Yaml(List<AppEnvRestriction.Yaml> appEnvRestrictions) {
      this.appEnvRestrictions = appEnvRestrictions;
    }
  }
}
