/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.infra;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.service.steps.ServiceStepOutcome;
import io.harness.exception.UnexpectedException;
import io.harness.steps.environment.EnvironmentOutcome;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Hex;

@OwnedBy(CDP)
@UtilityClass
public class InfrastructureKey {
  private static final String HASH_ALGORITHM = "SHA-1";
  private static final String INFRA_KEY_DELIMITER = "-";

  public String generate(ServiceStepOutcome service, EnvironmentOutcome env, String... params) {
    String formattedParams = String.join(INFRA_KEY_DELIMITER, params);
    String rawKey = String.join(INFRA_KEY_DELIMITER, service.getIdentifier(), env.getIdentifier(), formattedParams);
    return hashKey(rawKey.getBytes(UTF_8));
  }

  private String hashKey(byte[] key) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
      byte[] keyBytes = messageDigest.digest(key);
      return Hex.encodeHexString(keyBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new UnexpectedException(String.format("Algorithm %s not available", HASH_ALGORITHM), e);
    }
  }
}
