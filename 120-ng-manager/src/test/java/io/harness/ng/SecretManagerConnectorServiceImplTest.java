package io.harness.ng;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.rule.OwnerRule.PHOENIKX;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.connector.ConnectorDTO;
import io.harness.connector.ConnectorInfoDTO;
import io.harness.connector.ConnectorResponseDTO;
import io.harness.connector.services.ConnectorService;
import io.harness.delegate.beans.connector.ConnectorType;
import io.harness.delegate.beans.connector.vaultconnector.VaultConnectorDTO;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.SecretManagementException;
import io.harness.ng.core.api.NGSecretManagerService;
import io.harness.repositories.ConnectorRepository;
import io.harness.rule.Owner;
import io.harness.secretmanagerclient.dto.SecretManagerConfigDTO;
import io.harness.secretmanagerclient.dto.VaultConfigDTO;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(PL)
public class SecretManagerConnectorServiceImplTest extends CategoryTest {
  private static final String ACCOUNT = "account";
  private NGSecretManagerService ngSecretManagerService;
  private ConnectorService defaultConnectorService;
  private SecretManagerConnectorServiceImpl secretManagerConnectorService;
  private ConnectorRepository connectorRepository;

  @Before
  public void setup() {
    ngSecretManagerService = mock(NGSecretManagerService.class);
    defaultConnectorService = mock(ConnectorService.class);
    connectorRepository = mock(ConnectorRepository.class);
    secretManagerConnectorService =
        new SecretManagerConnectorServiceImpl(defaultConnectorService, ngSecretManagerService, connectorRepository);
  }

  private InvalidRequestException getInvalidRequestException() {
    return new InvalidRequestException("Invalid request");
  }

  private ConnectorDTO getRequestDTO() {
    ConnectorInfoDTO connectorInfo = ConnectorInfoDTO.builder().build();
    connectorInfo.setConnectorType(ConnectorType.VAULT);
    connectorInfo.setConnectorConfig(VaultConnectorDTO.builder()
                                         .vaultUrl("http://abc.com:8200")
                                         .secretEngineVersion(1)
                                         .renewalIntervalMinutes(10)
                                         .build());
    connectorInfo.setName("name");
    connectorInfo.setIdentifier("identifier");
    return ConnectorDTO.builder().connectorInfo(connectorInfo).build();
  }

  @Test
  @Owner(developers = PHOENIKX)
  @Category(UnitTests.class)
  public void testCreateSecretManagerConnector() {
    SecretManagerConfigDTO secretManagerConfigDTO = random(VaultConfigDTO.class);
    when(defaultConnectorService.get(any(), any(), any(), any())).thenReturn(Optional.empty());
    when(ngSecretManagerService.createSecretManager(any())).thenReturn(secretManagerConfigDTO);
    when(defaultConnectorService.create(any(), any())).thenReturn(null);
    when(connectorRepository.updateMultiple(any(), any())).thenReturn(null);
    ConnectorResponseDTO connectorDTO = secretManagerConnectorService.create(getRequestDTO(), ACCOUNT);
    assertThat(connectorDTO).isEqualTo(null);
    verify(defaultConnectorService).create(any(), any());
  }

  @Test
  @Owner(developers = PHOENIKX)
  @Category(UnitTests.class)
  public void testCreateSecretManagerConnectorShouldFail_ManagerReturnsNull() {
    when(defaultConnectorService.get(any(), any(), any(), any())).thenReturn(Optional.empty());
    when(ngSecretManagerService.createSecretManager(any())).thenReturn(null);
    when(connectorRepository.updateMultiple(any(), any())).thenReturn(null);
    try {
      secretManagerConnectorService.create(getRequestDTO(), ACCOUNT);
      fail("Should fail if execution reaches here");
    } catch (SecretManagementException exception) {
      // do nothing
    }
  }

  @Test
  @Owner(developers = PHOENIKX)
  @Category(UnitTests.class)
  public void testCreateSecretManagerConnectorShouldFail_exceptionFromManager() throws IOException {
    SecretManagerConfigDTO secretManagerConfigDTO = random(VaultConfigDTO.class);
    when(defaultConnectorService.get(any(), any(), any(), any())).thenReturn(Optional.empty());
    when(ngSecretManagerService.createSecretManager(any())).thenThrow(getInvalidRequestException());
    when(connectorRepository.updateMultiple(any(), any())).thenReturn(null);
    try {
      secretManagerConnectorService.create(getRequestDTO(), ACCOUNT);
      fail("Should fail if execution reaches here");
    } catch (InvalidRequestException exception) {
      // do nothing
    }
  }

  @Test
  @Owner(developers = PHOENIKX)
  @Category(UnitTests.class)
  public void updateSecretManager() {
    when(ngSecretManagerService.updateSecretManager(any(), any(), any(), any(), any()))
        .thenReturn(random(VaultConfigDTO.class));
    when(defaultConnectorService.update(any(), any())).thenReturn(null);
    when(connectorRepository.updateMultiple(any(), any())).thenReturn(null);
    try {
      secretManagerConnectorService.update(getRequestDTO(), ACCOUNT);
      fail("Should fail if execution reaches here");
    } catch (InvalidRequestException exception) {
      // do nothing
    }
  }

  @Test
  @Owner(developers = PHOENIKX)
  @Category(UnitTests.class)
  public void testDeleteSecretManager() {
    when(ngSecretManagerService.deleteSecretManager(any(), any(), any(), any())).thenReturn(true);
    when(defaultConnectorService.delete(any(), any(), any(), any())).thenReturn(true);
    boolean success = secretManagerConnectorService.delete(ACCOUNT, null, null, "identifier");
    assertThat(success).isEqualTo(true);
  }
}
