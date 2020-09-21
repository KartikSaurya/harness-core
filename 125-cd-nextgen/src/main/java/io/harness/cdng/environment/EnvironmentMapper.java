package io.harness.cdng.environment;

import io.harness.cdng.environment.yaml.EnvironmentYaml;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class EnvironmentMapper {
  public EnvironmentOutcome toOutcome(@Nonnull EnvironmentYaml environmentYaml) {
    return EnvironmentOutcome.builder()
        .identifier(environmentYaml.getIdentifier().getValue())
        .name(environmentYaml.getName() != null ? environmentYaml.getName().getValue() : "")
        .description(environmentYaml.getDescription() != null ? environmentYaml.getDescription().getValue() : "")
        .tags(environmentYaml.getTags())
        .type(environmentYaml.getType())
        .build();
  }
}
