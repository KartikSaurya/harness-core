/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.connector.heartbeat;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.ConnectorInfoDTO;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.ConnectorValidationParams;
import io.harness.delegate.beans.connector.azurekeyvaultconnector.AzureKeyVaultConnectorDTO;
import io.harness.delegate.beans.connector.azurekeyvaultconnector.AzureKeyVaultValidationParams;

@OwnedBy(PL)
public class AzureKeyVaultConnectorValidationParamsProvider
    extends SecretManagerConnectorValidationParamsProvider implements ConnectorValidationParamsProvider {
  @Override
  public ConnectorValidationParams getConnectorValidationParams(ConnectorInfoDTO connectorConfigDTO,
      String connectorName, String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    ConnectorConfigDTO connectorConfig =
        getDecryptedConnectorConfigDTO(connectorConfigDTO, accountIdentifier, orgIdentifier, projectIdentifier);
    return AzureKeyVaultValidationParams.builder()
        .azurekeyvaultConnectorDTO((AzureKeyVaultConnectorDTO) connectorConfig)
        .connectorName(connectorName)
        .build();
  }
}
