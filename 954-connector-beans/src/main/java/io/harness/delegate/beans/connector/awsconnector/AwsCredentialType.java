/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.beans.connector.awsconnector;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@OwnedBy(CDP)
public enum AwsCredentialType {
  @JsonProperty(AwsConstants.INHERIT_FROM_DELEGATE) INHERIT_FROM_DELEGATE(AwsConstants.INHERIT_FROM_DELEGATE),
  @JsonProperty(AwsConstants.MANUAL_CONFIG) MANUAL_CREDENTIALS(AwsConstants.MANUAL_CONFIG),
  @JsonProperty(AwsConstants.IRSA) IRSA(AwsConstants.IRSA);
  private final String displayName;

  AwsCredentialType(String displayName) {
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

  public static AwsCredentialType fromString(String typeEnum) {
    for (AwsCredentialType enumValue : AwsCredentialType.values()) {
      if (enumValue.getDisplayName().equals(typeEnum)) {
        return enumValue;
      }
    }
    throw new IllegalArgumentException("Invalid value: " + typeEnum);
  }
}
