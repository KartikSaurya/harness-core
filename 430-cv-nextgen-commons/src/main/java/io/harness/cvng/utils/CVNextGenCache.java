/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cvng.utils;

import static io.harness.persistence.HPersistence.DEFAULT_STORE;
import static io.harness.persistence.HQuery.excludeAuthority;

import io.harness.entity.HarnessApiKey;
import io.harness.entity.HarnessApiKey.HarnessApiKeyKeys;
import io.harness.persistence.HPersistence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CVNextGenCache {
  @Inject private HPersistence hPersistence;
  private LoadingCache<String, String> accountKeyCache =
      CacheBuilder.newBuilder()
          .maximumSize(10000)
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
              return (String) hPersistence.getCollection(DEFAULT_STORE, "accounts")
                  .findOne(new BasicDBObject("_id", key))
                  .get("accountKey");
            }
          });

  private LoadingCache<String, byte[]> apiKeyCache =
      CacheBuilder.newBuilder()
          .maximumSize(10000)
          .expireAfterWrite(1, TimeUnit.HOURS)
          .build(new CacheLoader<String, byte[]>() {
            @Override
            public byte[] load(String clientType) {
              HarnessApiKey harnessApiKey = hPersistence.createQuery(HarnessApiKey.class, excludeAuthority)
                                                .filter(HarnessApiKeyKeys.clientType, clientType)
                                                .get();
              if (harnessApiKey == null) {
                return null;
              }
              return harnessApiKey.getEncryptedKey();
            }
          });

  public String getAccountKey(String accountId) {
    try {
      return accountKeyCache.get(accountId);
    } catch (ExecutionException ex) {
      log.warn("Exception occurred in fetching account key {}", accountId, ex);
    }
    return null;
  }

  public byte[] getApiKey(String clientType) {
    try {
      return apiKeyCache.get(clientType);
    } catch (ExecutionException ex) {
      log.warn("Exception occurred in fetching account key {}", clientType, ex);
    }
    return null;
  }
}
