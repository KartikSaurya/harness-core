/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.connector.entities.embedded.awssecretmanager;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.entities.Connector;
import io.harness.delegate.beans.connector.awssecretmanager.AwsSecretManagerCredentialType;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(PL)
@Value
@Builder
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(innerTypeName = "AwsSecretManagerConnectorKeys")
@Entity(value = "connectors", noClassnameStored = true)
@Persistent
@TypeAlias("io.harness.connector.entities.embedded.awssecretmanager.AwsSecretManagerConnector")
public class AwsSecretManagerConnector extends Connector {
  String region;
  boolean isDefault;
  String secretNamePrefix;
  AwsSecretManagerCredentialType credentialType;
  AwsSecretManagerCredentialSpec credentialSpec;
  @Builder.Default Boolean harnessManaged = Boolean.FALSE;
}
