/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.connector.mappers.secretmanagermapper;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.entities.embedded.vaultconnector.VaultConnector;
import io.harness.connector.mappers.ConnectorEntityToDTOMapper;
import io.harness.delegate.beans.connector.vaultconnector.VaultConnectorDTO;
import io.harness.encryption.SecretRefHelper;

@OwnedBy(PL)
public class VaultEntityToDTO implements ConnectorEntityToDTOMapper<VaultConnectorDTO, VaultConnector> {
  @Override
  public VaultConnectorDTO createConnectorDTO(VaultConnector connector) {
    return VaultConnectorDTO.builder()
        .authToken(SecretRefHelper.createSecretRef(connector.getAuthTokenRef()))
        .isDefault(connector.isDefault())
        .isReadOnly(connector.isReadOnly())
        .vaultUrl(connector.getVaultUrl())
        .secretEngineName(connector.getSecretEngineName())
        .secretEngineVersion(connector.getSecretEngineVersion())
        .renewalIntervalMinutes(connector.getRenewalIntervalMinutes())
        .basePath(connector.getBasePath())
        .namespace(connector.getNamespace())
        .sinkPath(connector.getSinkPath())
        .useVaultAgent(connector.isUseVaultAgent())
        .secretEngineManuallyConfigured(connector.isSecretEngineManuallyConfigured())
        .appRoleId(connector.getAppRoleId())
        .secretId(SecretRefHelper.createSecretRef(connector.getSecretIdRef()))
        .delegateSelectors(connector.getDelegateSelectors())
        .build();
  }
}
