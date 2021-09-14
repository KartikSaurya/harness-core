/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.outbox;

import static io.harness.AuthorizationServiceHeader.PIPELINE_SERVICE;
import static io.harness.audit.beans.AuthenticationInfoDTO.fromSecurityPrincipal;
import static io.harness.security.PrincipalContextData.PRINCIPAL_CONTEXT;

import io.harness.ModuleType;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.audit.Action;
import io.harness.audit.beans.AuditEntry;
import io.harness.audit.beans.ResourceDTO;
import io.harness.audit.beans.ResourceScopeDTO;
import io.harness.audit.client.api.AuditClientService;
import io.harness.context.GlobalContext;
import io.harness.ng.core.ProjectScope;
import io.harness.outbox.OutboxEvent;
import io.harness.outbox.api.OutboxEventHandler;
import io.harness.pms.events.PipelineCreateEvent;
import io.harness.pms.events.PipelineDeleteEvent;
import io.harness.pms.events.PipelineOutboxEvents;
import io.harness.pms.events.PipelineUpdateEvent;
import io.harness.security.PrincipalContextData;
import io.harness.security.dto.Principal;
import io.harness.security.dto.ServicePrincipal;
import io.harness.utils.NGObjectMapperHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OwnedBy(HarnessTeam.PIPELINE)
public class PipelineOutboxEventHandler implements OutboxEventHandler {
  private ObjectMapper objectMapper;
  private final AuditClientService auditClientService;
  private final InputSetEventHandler inputSetEventHandler;
  @Inject
  PipelineOutboxEventHandler(AuditClientService auditClientService, InputSetEventHandler inputSetEventHandler) {
    this.objectMapper = NGObjectMapperHelper.NG_PIPELINE_OBJECT_MAPPER;
    this.auditClientService = auditClientService;
    this.inputSetEventHandler = inputSetEventHandler;
  }

  private boolean handlePipelineCreateEvent(OutboxEvent outboxEvent) throws IOException {
    GlobalContext globalContext = outboxEvent.getGlobalContext();
    String accountIdentifier = ((ProjectScope) outboxEvent.getResourceScope()).getAccountIdentifier();
    PipelineCreateEvent event = objectMapper.readValue(outboxEvent.getEventData(), PipelineCreateEvent.class);
    AuditEntry auditEntry = AuditEntry.builder()
                                .action(Action.CREATE)
                                .module(ModuleType.CORE)
                                .newYaml(event.getPipeline().getYaml())
                                .timestamp(outboxEvent.getCreatedAt())
                                .resource(ResourceDTO.fromResource(outboxEvent.getResource()))
                                .resourceScope(ResourceScopeDTO.fromResourceScope(outboxEvent.getResourceScope()))
                                .insertId(outboxEvent.getId())
                                .build();
    Principal principal = null;
    if (globalContext.get(PRINCIPAL_CONTEXT) == null) {
      principal = new ServicePrincipal(PIPELINE_SERVICE.getServiceId());
    } else if (globalContext.get(PRINCIPAL_CONTEXT) != null) {
      principal = ((PrincipalContextData) globalContext.get(PRINCIPAL_CONTEXT)).getPrincipal();
    }
    return auditClientService.publishAudit(auditEntry, fromSecurityPrincipal(principal), globalContext);
  }
  private boolean handlePipelineUpdateEvent(OutboxEvent outboxEvent) throws IOException {
    GlobalContext globalContext = outboxEvent.getGlobalContext();
    String accountIdentifier = ((ProjectScope) outboxEvent.getResourceScope()).getAccountIdentifier();
    PipelineUpdateEvent event = objectMapper.readValue(outboxEvent.getEventData(), PipelineUpdateEvent.class);
    AuditEntry auditEntry = AuditEntry.builder()
                                .action(Action.UPDATE)
                                .module(ModuleType.CORE)
                                .newYaml(event.getNewPipeline().getYaml())
                                .oldYaml(event.getOldPipeline().getYaml())
                                .timestamp(outboxEvent.getCreatedAt())
                                .resource(ResourceDTO.fromResource(outboxEvent.getResource()))
                                .resourceScope(ResourceScopeDTO.fromResourceScope(outboxEvent.getResourceScope()))
                                .insertId(outboxEvent.getId())
                                .build();
    Principal principal = null;
    if (globalContext.get(PRINCIPAL_CONTEXT) == null) {
      principal = new ServicePrincipal(PIPELINE_SERVICE.getServiceId());
    } else if (globalContext.get(PRINCIPAL_CONTEXT) != null) {
      principal = ((PrincipalContextData) globalContext.get(PRINCIPAL_CONTEXT)).getPrincipal();
    }
    return auditClientService.publishAudit(auditEntry, fromSecurityPrincipal(principal), globalContext);
  }

  private boolean handlePipelineDeleteEvent(OutboxEvent outboxEvent) throws IOException {
    GlobalContext globalContext = outboxEvent.getGlobalContext();
    String accountIdentifier = ((ProjectScope) outboxEvent.getResourceScope()).getAccountIdentifier();
    PipelineDeleteEvent event = objectMapper.readValue(outboxEvent.getEventData(), PipelineDeleteEvent.class);
    AuditEntry auditEntry = AuditEntry.builder()
                                .action(Action.DELETE)
                                .module(ModuleType.CORE)
                                .oldYaml(event.getPipeline().getYaml())
                                .timestamp(outboxEvent.getCreatedAt())
                                .resource(ResourceDTO.fromResource(outboxEvent.getResource()))
                                .resourceScope(ResourceScopeDTO.fromResourceScope(outboxEvent.getResourceScope()))
                                .insertId(outboxEvent.getId())
                                .build();
    Principal principal = null;
    if (globalContext.get(PRINCIPAL_CONTEXT) == null) {
      principal = new ServicePrincipal(PIPELINE_SERVICE.getServiceId());
    } else if (globalContext.get(PRINCIPAL_CONTEXT) != null) {
      principal = ((PrincipalContextData) globalContext.get(PRINCIPAL_CONTEXT)).getPrincipal();
    }
    return auditClientService.publishAudit(auditEntry, fromSecurityPrincipal(principal), globalContext);
  }
  @Override
  public boolean handle(OutboxEvent outboxEvent) {
    try {
      switch (outboxEvent.getEventType()) {
        case PipelineOutboxEvents.PIPELINE_CREATED:
          return handlePipelineCreateEvent(outboxEvent);
        case PipelineOutboxEvents.PIPELINE_UPDATED:
          return handlePipelineUpdateEvent(outboxEvent);
        case PipelineOutboxEvents.PIPELINE_DELETED:
          return handlePipelineDeleteEvent(outboxEvent);
        case PipelineOutboxEvents.INPUT_SET_CREATED:
          return inputSetEventHandler.handleInputSetCreateEvent(outboxEvent);
        case PipelineOutboxEvents.INPUT_SET_UPDATED:
          return inputSetEventHandler.handleInputSetUpdateEvent(outboxEvent);
        case PipelineOutboxEvents.INPUT_SET_DELETED:
          return inputSetEventHandler.handleInputSetDeleteEvent(outboxEvent);
        default:
          return false;
      }
    } catch (IOException ex) {
      return false;
    }
  }
}
