/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.filters;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.plancreator.execution.ExecutionElementConfig;
import io.harness.pms.pipeline.filter.PipelineFilter;
import io.harness.pms.plan.creation.PlanCreatorUtils;
import io.harness.pms.sdk.core.filter.creation.beans.FilterCreationContext;
import io.harness.pms.sdk.core.pipeline.filters.ChildrenFilterJsonCreator;
import io.harness.pms.yaml.YAMLFieldNameConstants;
import io.harness.pms.yaml.YamlField;
import io.harness.pms.yaml.YamlNode;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@OwnedBy(HarnessTeam.PIPELINE)
public class ExecutionPMSFilterJsonCreator extends ChildrenFilterJsonCreator<ExecutionElementConfig> {
  @Override
  public Class<ExecutionElementConfig> getFieldClass() {
    return ExecutionElementConfig.class;
  }

  @Override
  public Map<String, Set<String>> getSupportedTypes() {
    return Collections.singletonMap(YAMLFieldNameConstants.EXECUTION, Collections.singleton(PlanCreatorUtils.ANY_TYPE));
  }

  @Override
  public int getStageCount(FilterCreationContext filterCreationContext, Collection<YamlField> children) {
    return 0;
  }

  @Override
  public Map<String, YamlField> getDependencies(FilterCreationContext filterCreationContext) {
    List<YamlNode> yamlNodes = Optional
                                   .of(Preconditions
                                           .checkNotNull(filterCreationContext.getCurrentField().getNode().getField(
                                               YAMLFieldNameConstants.STEPS))
                                           .getNode()
                                           .asArray())
                                   .orElse(Collections.emptyList());
    List<YamlField> stepYamlFields = PlanCreatorUtils.getStepYamlFields(yamlNodes);
    return stepYamlFields.stream().collect(Collectors.toMap(field -> field.getNode().getUuid(), field -> field));
  }

  @Override
  public PipelineFilter getFilterForGivenField() {
    return null;
  }
}
