/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ng.core;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.accesscontrol.AccessControlAdminClient;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.NextGenConfiguration;
import io.harness.ng.core.invites.api.InviteService;
import io.harness.ng.core.invites.api.impl.InviteServiceImpl;
import io.harness.persistence.HPersistence;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@OwnedBy(PL)
@AllArgsConstructor
public class InviteModule extends AbstractModule {
  private final boolean isNGAuthUIEnabled;

  @Override
  protected void configure() {
    bind(InviteService.class).to(InviteServiceImpl.class);
    registerRequiredBindings();
  }

  @Provides
  @Named("userVerificationSecret")
  @Singleton
  protected String getUserVerificationSecret(NextGenConfiguration nextGenConfiguration) {
    return nextGenConfiguration.getNextGenConfig().getUserVerificationSecret();
  }

  @Provides
  @Named("isNgAuthUIEnabled")
  public boolean isNGAuthUIEnabled() {
    return isNGAuthUIEnabled;
  }

  @Provides
  @Singleton
  protected TransactionTemplate getTransactionTemplate(MongoTransactionManager mongoTransactionManager) {
    return new TransactionTemplate(mongoTransactionManager);
  }

  private void registerRequiredBindings() {
    requireBinding(HPersistence.class);
    requireBinding(AccessControlAdminClient.class);
  }
}
