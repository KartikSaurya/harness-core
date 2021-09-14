/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ng.core.entitydetail;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.HarnessStringUtils.nullIfEmpty;
import static io.harness.eventsframework.schemas.entity.EntityTypeProtoEnum.INPUT_SETS;
import static io.harness.eventsframework.schemas.entity.EntityTypeProtoEnum.TEMPLATE;

import io.harness.EntityType;
import io.harness.beans.IdentifierRef;
import io.harness.beans.InputSetReference;
import io.harness.beans.NGTemplateReference;
import io.harness.common.EntityReference;
import io.harness.encryption.Scope;
import io.harness.eventsframework.schemas.entity.EntityDetailProtoDTO;
import io.harness.eventsframework.schemas.entity.EntityTypeProtoEnum;
import io.harness.eventsframework.schemas.entity.IdentifierRefProtoDTO;
import io.harness.eventsframework.schemas.entity.InputSetReferenceProtoDTO;
import io.harness.eventsframework.schemas.entity.ScopeProtoEnum;
import io.harness.eventsframework.schemas.entity.TemplateReferenceProtoDTO;
import io.harness.exception.UnknownEnumTypeException;
import io.harness.ng.core.EntityDetail;
import io.harness.ng.core.event.EventProtoToEntityHelper;

import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class EntityDetailProtoToRestMapper {
  public EntityDetail createEntityDetailDTO(EntityDetailProtoDTO entityDetail) {
    return EntityDetail.builder()
        .name(entityDetail.getName())
        .entityRef(createEntityReference(entityDetail))
        .type(mapEventToRestEntityType(entityDetail.getType()))
        .build();
  }

  public List<EntityDetail> createEntityDetailsDTO(List<EntityDetailProtoDTO> entityDetails) {
    if (isEmpty(entityDetails)) {
      return Collections.emptyList();
    }
    return entityDetails.stream().map(this::createEntityDetailDTO).collect(Collectors.toList());
  }

  private EntityReference createEntityReference(EntityDetailProtoDTO entityDetail) {
    if (entityDetail.getType() == INPUT_SETS) {
      return createInputSetRef(entityDetail.getInputSetRef());
    } else if (entityDetail.getType() == TEMPLATE) {
      return createTemplateRef(entityDetail.getTemplateRef());
    } else {
      return createIdentifierRef(entityDetail.getIdentifierRef());
    }
  }

  private EntityReference createIdentifierRef(IdentifierRefProtoDTO identifierRef) {
    return IdentifierRef.builder()
        .accountIdentifier(identifierRef.getAccountIdentifier().getValue())
        .orgIdentifier(nullIfEmpty(identifierRef.getOrgIdentifier().getValue()))
        .projectIdentifier(nullIfEmpty(identifierRef.getProjectIdentifier().getValue()))
        .scope(mapEventToRestScopeEnum(identifierRef.getScope()))
        .identifier(identifierRef.getIdentifier().getValue())
        .metadata(identifierRef.getMetadataMap())
        .build();
  }

  private Scope mapEventToRestScopeEnum(ScopeProtoEnum scope) {
    switch (scope) {
      case ACCOUNT:
        return Scope.ACCOUNT;
      case ORG:
        return Scope.ORG;
      case PROJECT:
        return Scope.PROJECT;
      case UNKNOWN:
        return Scope.UNKNOWN;
      case UNRECOGNIZED:
      default:
        throw new UnknownEnumTypeException("scope", String.valueOf(scope));
    }
  }

  private EntityReference createInputSetRef(InputSetReferenceProtoDTO inputSetReference) {
    return InputSetReference.builder()
        .accountIdentifier(inputSetReference.getAccountIdentifier().getValue())
        .identifier(inputSetReference.getIdentifier().getValue())
        .orgIdentifier(nullIfEmpty(inputSetReference.getOrgIdentifier().getValue()))
        .projectIdentifier(nullIfEmpty(inputSetReference.getProjectIdentifier().getValue()))
        .pipelineIdentifier(inputSetReference.getPipelineIdentifier().getValue())
        .build();
  }

  private NGTemplateReference createTemplateRef(TemplateReferenceProtoDTO templateReferenceProtoDTO) {
    return NGTemplateReference.builder()
        .accountIdentifier(templateReferenceProtoDTO.getAccountIdentifier().getValue())
        .orgIdentifier(templateReferenceProtoDTO.getOrgIdentifier().getValue())
        .projectIdentifier(templateReferenceProtoDTO.getProjectIdentifier().getValue())
        .identifier(templateReferenceProtoDTO.getIdentifier().getValue())
        .versionLabel(templateReferenceProtoDTO.getVersionLabel().getValue())
        .scope(mapEventToRestScopeEnum(templateReferenceProtoDTO.getScope()))
        .build();
  }

  private EntityType mapEventToRestEntityType(EntityTypeProtoEnum type) {
    Map<EntityTypeProtoEnum, EntityType> mappingBetweenProtoAndActualEnum =
        EventProtoToEntityHelper.getEntityTypeProtoEnumToRestEnumMap();
    if (mappingBetweenProtoAndActualEnum.containsKey(type)) {
      return mappingBetweenProtoAndActualEnum.get(type);
    } else {
      throw new UnknownEnumTypeException("entityType", String.valueOf(type));
    }
  }
}
