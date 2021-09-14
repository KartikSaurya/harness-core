/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.graphql.datafetcher.application;

import static io.harness.beans.FeatureName.WEBHOOK_TRIGGER_AUTHORIZATION;

import static software.wings.security.PermissionAttribute.PermissionType.MANAGE_APPLICATIONS;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
import io.harness.exception.InvalidRequestException;
import io.harness.ff.FeatureFlagService;

import software.wings.beans.Application;
import software.wings.graphql.datafetcher.BaseMutatorDataFetcher;
import software.wings.graphql.datafetcher.MutationContext;
import software.wings.graphql.schema.mutation.application.input.QLCreateApplicationInput;
import software.wings.graphql.schema.mutation.application.payload.QLCreateApplicationPayload;
import software.wings.graphql.schema.type.QLApplication;
import software.wings.security.PermissionAttribute;
import software.wings.security.annotations.AuthRule;
import software.wings.service.intfc.AppService;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public class CreateApplicationDataFetcher
    extends BaseMutatorDataFetcher<QLCreateApplicationInput, QLCreateApplicationPayload> {
  private AppService appService;
  private FeatureFlagService featureFlagService;

  @Inject
  public CreateApplicationDataFetcher(AppService appService, FeatureFlagService featureFlagService) {
    super(QLCreateApplicationInput.class, QLCreateApplicationPayload.class);
    this.appService = appService;
    this.featureFlagService = featureFlagService;
  }

  private Application prepareApplication(QLCreateApplicationInput qlApplicationInput, String accountId) {
    return Application.Builder.anApplication()
        .name(qlApplicationInput.getName())
        .description(qlApplicationInput.getDescription())
        .accountId(accountId)
        .isManualTriggerAuthorized(qlApplicationInput.getIsManualTriggerAuthorized())
        .build();
  }
  private QLApplication prepareQLApplication(Application savedApplication) {
    return ApplicationController.populateQLApplication(savedApplication, QLApplication.builder()).build();
  }

  @Override
  @AuthRule(permissionType = MANAGE_APPLICATIONS, action = PermissionAttribute.Action.CREATE)
  protected QLCreateApplicationPayload mutateAndFetch(
      QLCreateApplicationInput parameter, MutationContext mutationContext) {
    if (Boolean.TRUE.equals(parameter.getIsManualTriggerAuthorized())
        && !featureFlagService.isEnabled(WEBHOOK_TRIGGER_AUTHORIZATION, mutationContext.getAccountId())) {
      throw new InvalidRequestException("Please enable feature flag to authorize manual triggers");
    }
    final Application savedApplication = appService.save(prepareApplication(parameter, mutationContext.getAccountId()));
    return QLCreateApplicationPayload.builder()
        .clientMutationId(parameter.getClientMutationId())
        .application(prepareQLApplication(savedApplication))
        .build();
  }
}
