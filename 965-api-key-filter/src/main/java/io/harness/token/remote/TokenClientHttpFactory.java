/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.token.remote;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.remote.client.AbstractHttpClientFactory;
import io.harness.remote.client.ClientMode;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.security.ServiceTokenGenerator;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Singleton
@Slf4j
@OwnedBy(HarnessTeam.PL)
public class TokenClientHttpFactory extends AbstractHttpClientFactory implements Provider<TokenClient> {
  public TokenClientHttpFactory(ServiceHttpClientConfig ngManagerConfig, String serviceSecret,
      ServiceTokenGenerator tokenGenerator, String clientId, ClientMode clientMode) {
    super(ngManagerConfig, serviceSecret, tokenGenerator, null, clientId, false, clientMode);
    log.info("TokenClientFactory ngManagerConfig: {}", ngManagerConfig);
  }

  @Override
  public TokenClient get() {
    return getRetrofit().create(TokenClient.class);
  }
}
