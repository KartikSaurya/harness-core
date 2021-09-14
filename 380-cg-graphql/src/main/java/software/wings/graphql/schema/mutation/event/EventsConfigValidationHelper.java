/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.graphql.schema.mutation.event;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.CgEventConfig;
import io.harness.beans.CgEventRule;
import io.harness.exception.InvalidRequestException;

import software.wings.beans.Pipeline;
import software.wings.beans.Pipeline.PipelineKeys;
import software.wings.dl.WingsPersistence;

import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@OwnedBy(HarnessTeam.CDC)
public class EventsConfigValidationHelper {
  @Inject private WingsPersistence wingsPersistence;

  public void validatePipelineIds(CgEventConfig cgEventConfig, String accountId, String appId) {
    CgEventRule cgEventRule = cgEventConfig.getRule();
    if (cgEventRule == null || cgEventRule.getType() == null) {
      return;
    }
    if (cgEventRule.getType().equals(CgEventRule.CgRuleType.PIPELINE)) {
      CgEventRule.PipelineRule pipelineRule = cgEventRule.getPipelineRule();
      if (pipelineRule == null || pipelineRule.isAllPipelines() || isEmpty(pipelineRule.getPipelineIds())) {
        return;
      }
      List<String> pipelineIds = pipelineRule.getPipelineIds();
      Set<String> pipelineIdSet = wingsPersistence.createQuery(Pipeline.class)
                                      .filter(PipelineKeys.accountId, accountId)
                                      .filter(PipelineKeys.appId, appId)
                                      .project(PipelineKeys.uuid, true)
                                      .asList()
                                      .stream()
                                      .map(pipeline -> pipeline.getUuid())
                                      .collect(Collectors.toSet());
      List<String> invalidIds =
          pipelineIds.stream().filter(id -> !pipelineIdSet.contains(id)).collect(Collectors.toList());
      if (isNotEmpty(invalidIds)) {
        throw new InvalidRequestException("The following pipeline ids are invalid :" + invalidIds.toString());
      }
    }
  }
}
