/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.connector.validator;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import static software.wings.beans.TaskType.GCP_TASK;

import io.harness.annotations.dev.OwnedBy;
import io.harness.connector.ConnectorValidationResult;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.gcpconnector.GcpConnectorCredentialDTO;
import io.harness.delegate.beans.connector.gcpconnector.GcpConnectorDTO;
import io.harness.delegate.beans.connector.gcpconnector.GcpManualDetailsDTO;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.gcp.GcpTaskType;
import io.harness.delegate.task.gcp.request.GcpTaskParameters;
import io.harness.delegate.task.gcp.request.GcpValidationRequest;
import io.harness.delegate.task.gcp.request.GcpValidationRequest.GcpValidationRequestBuilder;
import io.harness.delegate.task.gcp.response.GcpValidationTaskResponse;
import io.harness.exception.InvalidRequestException;

@OwnedBy(CDP)
public class GcpConnectorValidator extends AbstractConnectorValidator {
  @Override
  public <T extends ConnectorConfigDTO> TaskParameters getTaskParameters(
      T connectorConfig, String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    GcpConnectorDTO gcpConnectorDTO = (GcpConnectorDTO) connectorConfig;
    GcpConnectorCredentialDTO gcpConnector = gcpConnectorDTO.getCredential();
    final GcpValidationRequestBuilder<?, ?> gcpValidationRequestBuilder = GcpValidationRequest.builder();
    switch (gcpConnector.getGcpCredentialType()) {
      case MANUAL_CREDENTIALS:
        final GcpManualDetailsDTO gcpDetailsDTO = (GcpManualDetailsDTO) gcpConnector.getConfig();
        GcpValidationRequest manualCredentialsRequest = gcpValidationRequestBuilder.gcpManualDetailsDTO(gcpDetailsDTO)
                                                            .encryptionDetails(super.getEncryptionDetail(gcpDetailsDTO,
                                                                accountIdentifier, orgIdentifier, projectIdentifier))
                                                            .delegateSelectors(gcpConnectorDTO.getDelegateSelectors())
                                                            .build();
        return GcpTaskParameters.builder()
            .gcpRequest(manualCredentialsRequest)
            .gcpTaskType(GcpTaskType.VALIDATE)
            .build();
      case INHERIT_FROM_DELEGATE:
        GcpValidationRequest inheritDelegateRequest =
            gcpValidationRequestBuilder.delegateSelectors(gcpConnectorDTO.getDelegateSelectors()).build();
        return GcpTaskParameters.builder().gcpRequest(inheritDelegateRequest).gcpTaskType(GcpTaskType.VALIDATE).build();
      default:
        throw new InvalidRequestException("Invalid credential type: " + gcpConnector.getGcpCredentialType());
    }
  }
  @Override
  public String getTaskType() {
    return GCP_TASK.name();
  }
  @Override
  public ConnectorValidationResult validate(ConnectorConfigDTO connectorDTO, String accountIdentifier,
      String orgIdentifier, String projectIdentifier, String identifier) {
    final GcpValidationTaskResponse gcpValidationTaskResponse = (GcpValidationTaskResponse) super.validateConnector(
        connectorDTO, accountIdentifier, orgIdentifier, projectIdentifier, identifier);
    return gcpValidationTaskResponse.getConnectorValidationResult();
  }
}
