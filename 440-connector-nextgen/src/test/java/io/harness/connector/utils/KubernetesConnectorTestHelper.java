/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.connector.utils;

import static io.harness.delegate.beans.connector.k8Connector.KubernetesCredentialType.MANUAL_CREDENTIALS;
import static io.harness.encryption.Scope.ACCOUNT;
import static io.harness.encryption.SecretRefData.SECRET_DOT_DELIMINITER;

import io.harness.connector.entities.Connector;
import io.harness.connector.entities.embedded.kubernetescluster.K8sUserNamePassword;
import io.harness.connector.entities.embedded.kubernetescluster.KubernetesClusterConfig;
import io.harness.connector.entities.embedded.kubernetescluster.KubernetesClusterDetails;
import io.harness.delegate.beans.connector.ConnectorType;
import io.harness.delegate.beans.connector.k8Connector.KubernetesAuthType;
import io.harness.encryption.Scope;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KubernetesConnectorTestHelper {
  public Connector createK8sConnector(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String identifier, Scope scope) {
    String masterURL = "masterURL";
    String userName = "userName";
    String passwordIdentifier = "passwordIdentifier";
    String passwordRef = ACCOUNT + SECRET_DOT_DELIMINITER + passwordIdentifier;
    K8sUserNamePassword k8sUserNamePassword =
        K8sUserNamePassword.builder().userName(userName).passwordRef(passwordRef).build();
    KubernetesClusterDetails kubernetesClusterDetails = KubernetesClusterDetails.builder()
                                                            .masterUrl(masterURL)
                                                            .authType(KubernetesAuthType.USER_PASSWORD)
                                                            .auth(k8sUserNamePassword)
                                                            .build();
    Connector connector = KubernetesClusterConfig.builder()
                              .credentialType(MANUAL_CREDENTIALS)
                              .credential(kubernetesClusterDetails)
                              .build();
    connector.setAccountIdentifier(accountIdentifier);
    connector.setOrgIdentifier(orgIdentifier);
    connector.setProjectIdentifier(projectIdentifier);
    connector.setIdentifier(identifier);
    connector.setScope(scope);
    connector.setType(ConnectorType.KUBERNETES_CLUSTER);
    return connector;
  }
}
