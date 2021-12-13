package io.harness.pms.events;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.audit.ResourceType;
import io.harness.event.Event;
import io.harness.ng.core.ProjectScope;
import io.harness.ng.core.Resource;
import io.harness.ng.core.ResourceScope;
import io.harness.pms.ngpipeline.inputset.beans.entity.InputSetEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@OwnedBy(PIPELINE)
@Getter
@Builder
@AllArgsConstructor
public class InputSetDeleteEvent implements Event {
  private String accountIdentifier;
  private String orgIdentifier;
  private String projectIdentifier;
  private String pipelineIdentifier;
  private InputSetEntity inputSet;

  @JsonIgnore
  @Override
  public ResourceScope getResourceScope() {
    return new ProjectScope(accountIdentifier, orgIdentifier, projectIdentifier);
  }

  @JsonIgnore
  @Override
  public Resource getResource() {
    return Resource.builder()
        .identifier(inputSet.getIdentifier())
        .type(ResourceType.INPUT_SET.name())
        .labels(ImmutableMap.<String, String>builder().put("pipelineIdentifier", pipelineIdentifier).build())
        .build();
  }

  @JsonIgnore
  @Override
  public String getEventType() {
    return PipelineOutboxEvents.INPUT_SET_DELETED;
  }
}
