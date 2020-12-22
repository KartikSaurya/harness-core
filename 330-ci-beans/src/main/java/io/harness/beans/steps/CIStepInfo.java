package io.harness.beans.steps;

import io.harness.executionplan.plancreator.beans.GenericStepInfo;
import io.harness.yaml.core.StepSpecType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonDeserialize
public interface CIStepInfo extends StepSpecType, GenericStepInfo {
  int MIN_RETRY = 0;
  int MAX_RETRY = 5;
  int MIN_TIMEOUT = 1;
  int MAX_TIMEOUT = 999;

  TypeInfo getNonYamlInfo();
  int getRetry();
  int getTimeout();
  String getName();

  // TODO: implement this when we support graph section in yaml
  default List<String> getDependencies() {
    return null;
  }
}
