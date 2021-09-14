/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.resourcegroupclient;

import io.harness.remote.client.ClientMode;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.resourcegroupclient.remote.ResourceGroupClient;
import io.harness.resourcegroupclient.remote.ResourceGroupHttpClientFactory;
import io.harness.security.ServiceTokenGenerator;
import io.harness.serializer.kryo.KryoConverterFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class ResourceGroupClientModule extends AbstractModule {
  private final ServiceHttpClientConfig resourceGroupClientConfig;
  private final String serviceSecret;
  private final String clientId;

  public ResourceGroupClientModule(
      ServiceHttpClientConfig resourceGroupClientConfig, String serviceSecret, String clientId) {
    this.resourceGroupClientConfig = resourceGroupClientConfig;
    this.serviceSecret = serviceSecret;
    this.clientId = clientId;
  }

  @Provides
  private ResourceGroupHttpClientFactory resourceGroupHttpClientFactory(KryoConverterFactory kryoConverterFactory) {
    return new ResourceGroupHttpClientFactory(resourceGroupClientConfig, serviceSecret, new ServiceTokenGenerator(),
        kryoConverterFactory, clientId, ClientMode.NON_PRIVILEGED);
  }

  @Provides
  @Named("PRIVILEGED")
  private ResourceGroupHttpClientFactory resourceGroupAdminHttpClientFactory(
      KryoConverterFactory kryoConverterFactory) {
    return new ResourceGroupHttpClientFactory(resourceGroupClientConfig, serviceSecret, new ServiceTokenGenerator(),
        kryoConverterFactory, clientId, ClientMode.PRIVILEGED);
  }

  @Override
  protected void configure() {
    bind(ResourceGroupClient.class).toProvider(ResourceGroupHttpClientFactory.class).in(Scopes.SINGLETON);
    bind(ResourceGroupClient.class)
        .annotatedWith(Names.named(ClientMode.PRIVILEGED.name()))
        .toProvider(Key.get(ResourceGroupHttpClientFactory.class, Names.named(ClientMode.PRIVILEGED.name())))
        .in(Scopes.SINGLETON);
  }
}
