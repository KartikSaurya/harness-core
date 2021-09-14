/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.graphql.datafetcher.trigger;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.logging.AccountLogContext;
import io.harness.logging.AutoLogContext;

import software.wings.beans.trigger.Trigger;
import software.wings.graphql.datafetcher.BaseMutatorDataFetcher;
import software.wings.graphql.datafetcher.MutationContext;
import software.wings.graphql.schema.type.trigger.QLCreateOrUpdateTriggerInput;
import software.wings.graphql.schema.type.trigger.QLTriggerPayload;
import software.wings.security.PermissionAttribute;
import software.wings.security.annotations.AuthRule;
import software.wings.service.intfc.TriggerService;

import com.google.inject.Inject;

@OwnedBy(HarnessTeam.CDC)
public class CreateTriggerDataFetcher extends BaseMutatorDataFetcher<QLCreateOrUpdateTriggerInput, QLTriggerPayload> {
  @Inject TriggerController triggerController;
  private TriggerService triggerService;

  @Inject
  public CreateTriggerDataFetcher(TriggerService triggerService) {
    super(QLCreateOrUpdateTriggerInput.class, QLTriggerPayload.class);
    this.triggerService = triggerService;
  }

  @Override
  @AuthRule(permissionType = PermissionAttribute.PermissionType.LOGGED_IN)
  protected QLTriggerPayload mutateAndFetch(QLCreateOrUpdateTriggerInput parameter, MutationContext mutationContext) {
    try (AutoLogContext ignore0 =
             new AccountLogContext(mutationContext.getAccountId(), AutoLogContext.OverrideBehavior.OVERRIDE_ERROR)) {
      final Trigger savedTrigger =
          triggerService.save(triggerController.prepareTrigger(parameter, mutationContext.getAccountId()));
      return triggerController.prepareQLTrigger(
          savedTrigger, parameter.getClientMutationId(), mutationContext.getAccountId());
    }
  }
}
