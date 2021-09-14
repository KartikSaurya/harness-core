/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.user;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.remote.client.ClientMode;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.security.ServiceTokenGenerator;
import io.harness.serializer.kryo.KryoConverterFactory;
import io.harness.user.remote.UserClient;
import io.harness.user.remote.UserHttpClientFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

@OwnedBy(PL)
public class UserClientModule extends AbstractModule {
  private static UserClientModule instance;
  private final ServiceHttpClientConfig serviceHttpClientConfig;
  private final String serviceSecret;
  private final String clientId;

  public UserClientModule(ServiceHttpClientConfig serviceHttpClientConfig, String serviceSecret, String clientId) {
    this.serviceHttpClientConfig = serviceHttpClientConfig;
    this.serviceSecret = serviceSecret;
    this.clientId = clientId;
  }

  public static UserClientModule getInstance(
      ServiceHttpClientConfig serviceHttpClientConfig, String serviceSecret, String clientId) {
    if (instance == null) {
      instance = new UserClientModule(serviceHttpClientConfig, serviceSecret, clientId);
    }

    return instance;
  }

  @Provides
  private UserHttpClientFactory userClientFactory(KryoConverterFactory kryoConverterFactory) {
    return new UserHttpClientFactory(serviceHttpClientConfig, serviceSecret, new ServiceTokenGenerator(),
        kryoConverterFactory, clientId, ClientMode.NON_PRIVILEGED);
  }

  @Provides
  @Named("PRIVILEGED")
  private UserHttpClientFactory privilegedUserClientFactory() {
    return new UserHttpClientFactory(
        serviceHttpClientConfig, serviceSecret, new ServiceTokenGenerator(), null, clientId, ClientMode.PRIVILEGED);
  }

  @Override
  protected void configure() {
    bind(UserClient.class).toProvider(UserHttpClientFactory.class).in(Scopes.SINGLETON);
    bind(UserClient.class)
        .annotatedWith(Names.named(ClientMode.PRIVILEGED.name()))
        .toProvider(privilegedUserClientFactory())
        .in(Scopes.SINGLETON);
  }
}
