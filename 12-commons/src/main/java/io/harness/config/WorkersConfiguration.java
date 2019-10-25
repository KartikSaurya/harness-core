package io.harness.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
public class WorkersConfiguration implements ActiveConfigValidator {
  @JsonProperty("active") Map<String, Boolean> active;
  public boolean confirmWorkerIsActive(Class cls) {
    return isActive(cls, active);
  }
}
