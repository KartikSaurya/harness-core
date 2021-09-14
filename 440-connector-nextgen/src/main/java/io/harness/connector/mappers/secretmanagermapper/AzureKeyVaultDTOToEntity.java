/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.connector.mappers.secretmanagermapper;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.entities.embedded.azurekeyvaultconnector.AzureKeyVaultConnector;
import io.harness.connector.mappers.ConnectorDTOToEntityMapper;
import io.harness.delegate.beans.connector.azurekeyvaultconnector.AzureKeyVaultConnectorDTO;
import io.harness.encryption.SecretRefHelper;

@OwnedBy(PL)
public class AzureKeyVaultDTOToEntity
    implements ConnectorDTOToEntityMapper<AzureKeyVaultConnectorDTO, AzureKeyVaultConnector> {
  @Override
  public AzureKeyVaultConnector toConnectorEntity(AzureKeyVaultConnectorDTO connectorDTO) {
    return AzureKeyVaultConnector.builder()
        .isDefault(connectorDTO.isDefault())
        .clientId(connectorDTO.getClientId())
        .tenantId(connectorDTO.getTenantId())
        .vaultName(connectorDTO.getVaultName())
        .secretKeyRef(SecretRefHelper.getSecretConfigString(connectorDTO.getSecretKey()))
        .subscription(connectorDTO.getSubscription())
        .azureEnvironmentType(connectorDTO.getAzureEnvironmentType())
        .build();
  }
}
