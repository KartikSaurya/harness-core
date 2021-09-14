/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.beans.connector.awssecretmanager;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@OwnedBy(HarnessTeam.PL)
public enum AwsSecretManagerCredentialType {
  @JsonProperty(AwsSecretManagerConstants.ASSUME_IAM_ROLE) ASSUME_IAM_ROLE(AwsSecretManagerConstants.ASSUME_IAM_ROLE),
  @JsonProperty(AwsSecretManagerConstants.ASSUME_STS_ROLE) ASSUME_STS_ROLE(AwsSecretManagerConstants.ASSUME_STS_ROLE),
  @JsonProperty(AwsSecretManagerConstants.MANUAL_CONFIG) MANUAL_CONFIG(AwsSecretManagerConstants.MANUAL_CONFIG);
  private final String displayName;

  AwsSecretManagerCredentialType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }

  @JsonValue
  final String displayName() {
    return this.displayName;
  }

  public static AwsSecretManagerCredentialType fromString(String typeEnum) {
    for (AwsSecretManagerCredentialType enumValue : AwsSecretManagerCredentialType.values()) {
      if (enumValue.getDisplayName().equals(typeEnum)) {
        return enumValue;
      }
    }
    throw new IllegalArgumentException("Invalid value: " + typeEnum);
  }
}
