/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.service.steps;

import io.harness.accesscontrol.Principal;
import io.harness.accesscontrol.clients.AccessControlClient;
import io.harness.accesscontrol.clients.Resource;
import io.harness.accesscontrol.clients.ResourceScope;
import io.harness.accesscontrol.principals.PrincipalType;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.service.beans.ServiceConfig;
import io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants;
import io.harness.common.ParameterFieldHelper;
import io.harness.data.structure.EmptyPredicate;
import io.harness.eventsframework.schemas.entity.EntityDetailProtoDTO;
import io.harness.exception.InvalidRequestException;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.ng.core.mapper.TagMapper;
import io.harness.ng.core.service.entity.ServiceEntity;
import io.harness.ng.core.service.services.ServiceEntityService;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.plan.ExecutionPrincipalInfo;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.rbac.PipelineRbacHelper;
import io.harness.pms.rbac.PrincipalTypeProtoToPrincipalTypeMapper;
import io.harness.pms.sdk.core.plan.creation.yaml.StepOutcomeGroup;
import io.harness.pms.sdk.core.steps.executables.SyncExecutable;
import io.harness.pms.sdk.core.steps.io.PassThroughData;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.tags.TagUtils;
import io.harness.rbac.CDNGRbacPermissions;
import io.harness.steps.EntityReferenceExtractorUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Optional;
import java.util.Set;

@OwnedBy(HarnessTeam.CDC)
public class ServiceStep implements SyncExecutable<ServiceStepParameters> {
  public static final StepType STEP_TYPE =
      StepType.newBuilder().setType(ExecutionNodeType.SERVICE.getName()).setStepCategory(StepCategory.STEP).build();

  @Inject private EntityReferenceExtractorUtils entityReferenceExtractorUtils;
  @Inject @Named("PRIVILEGED") private AccessControlClient accessControlClient;
  @Inject private PipelineRbacHelper pipelineRbacHelper;
  @Inject private ServiceEntityService serviceEntityService;

  @Override
  public Class<ServiceStepParameters> getStepParametersClass() {
    return ServiceStepParameters.class;
  }

  @Override
  public StepResponse executeSync(Ambiance ambiance, ServiceStepParameters stepParameters,
      StepInputPackage inputPackage, PassThroughData passThroughData) {
    validateResources(ambiance, stepParameters);
    ServiceEntity serviceEntity = getServiceEntity(ambiance, stepParameters);
    serviceEntityService.upsert(serviceEntity);
    return StepResponse.builder()
        .status(Status.SUCCEEDED)
        .stepOutcome(StepResponse.StepOutcome.builder()
                         .name(OutcomeExpressionConstants.SERVICE)
                         .outcome(ServiceStepOutcome.fromServiceEntity(stepParameters.getType(), serviceEntity))
                         .group(StepOutcomeGroup.STAGE.name())
                         .build())
        .build();
  }

  private void validateResources(Ambiance ambiance, ServiceStepParameters stepParameters) {
    String accountIdentifier = AmbianceUtils.getAccountId(ambiance);
    String orgIdentifier = AmbianceUtils.getOrgIdentifier(ambiance);
    String projectIdentifier = AmbianceUtils.getProjectIdentifier(ambiance);
    ExecutionPrincipalInfo executionPrincipalInfo = ambiance.getMetadata().getPrincipalInfo();
    String principal = executionPrincipalInfo.getPrincipal();
    if (EmptyPredicate.isEmpty(principal)) {
      return;
    }

    PrincipalType principalType = PrincipalTypeProtoToPrincipalTypeMapper.convertToAccessControlPrincipalType(
        executionPrincipalInfo.getPrincipalType());
    ServiceConfig serviceConfig = stepParameters.getServiceConfigInternal().getValue();
    Set<EntityDetailProtoDTO> entityDetails =
        entityReferenceExtractorUtils.extractReferredEntities(ambiance, serviceConfig);
    pipelineRbacHelper.checkRuntimePermissions(ambiance, entityDetails);
    if (stepParameters.getServiceRefInternal() == null
        || EmptyPredicate.isEmpty(stepParameters.getServiceRefInternal().getValue())) {
      accessControlClient.checkForAccessOrThrow(Principal.of(principalType, principal),
          ResourceScope.of(accountIdentifier, orgIdentifier, projectIdentifier), Resource.of("SERVICE", null),
          CDNGRbacPermissions.SERVICE_CREATE_PERMISSION, "Validation for Service Step failed");
    }
  }

  // NOTE: Returned service entity shouldn't contain a version. Multiple stages running in parallel might see
  // DuplicateKeyException if they're trying to deploy the same service.
  private ServiceEntity getServiceEntity(Ambiance ambiance, ServiceStepParameters stepParameters) {
    String accountId = AmbianceUtils.getAccountId(ambiance);
    String projectIdentifier = AmbianceUtils.getProjectIdentifier(ambiance);
    String orgIdentifier = AmbianceUtils.getOrgIdentifier(ambiance);

    if (stepParameters.getServiceRefInternal() != null
        && EmptyPredicate.isNotEmpty(stepParameters.getServiceRefInternal().getValue())) {
      String serviceIdentifier = stepParameters.getServiceRefInternal().getValue();
      Optional<ServiceEntity> serviceEntity =
          serviceEntityService.get(accountId, orgIdentifier, projectIdentifier, serviceIdentifier, false);
      if (serviceEntity.isPresent()) {
        ServiceEntity finalServiceEntity = serviceEntity.get();
        finalServiceEntity.setVersion(null);
        return finalServiceEntity;
      } else {
        throw new InvalidRequestException("Service with identifier " + serviceIdentifier + " does not exist");
      }
    }

    TagUtils.removeUuidFromTags(stepParameters.getTags());

    return ServiceEntity.builder()
        .identifier(stepParameters.getIdentifier())
        .name(stepParameters.getName())
        .description(ParameterFieldHelper.getParameterFieldValueHandleValueNull(stepParameters.getDescription()))
        .projectIdentifier(projectIdentifier)
        .orgIdentifier(orgIdentifier)
        .accountId(accountId)
        .tags(TagMapper.convertToList(stepParameters.getTags()))
        .build();
  }
}
