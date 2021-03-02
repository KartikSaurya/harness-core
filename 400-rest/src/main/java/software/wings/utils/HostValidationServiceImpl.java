package software.wings.utils;

import static io.harness.govern.Switch.noop;
import static io.harness.shell.SshHelperUtils.normalizeError;
import static io.harness.winrm.WinRmHelperUtils.buildErrorDetailsFromWinRmClientException;

import static software.wings.beans.command.CommandExecutionContext.Builder.aCommandExecutionContext;
import static software.wings.common.Constants.WINDOWS_HOME_DIR;
import static software.wings.utils.SshHelperUtils.createSshSessionConfig;

import io.harness.beans.ExecutionStatus;
import io.harness.beans.FeatureName;
import io.harness.eraro.ErrorCode;
import io.harness.eraro.ResponseMessage;
import io.harness.ff.FeatureFlagService;
import io.harness.logging.NoopExecutionCallback;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.shell.SshSessionConfig;
import io.harness.shell.SshSessionFactory;

import software.wings.annotation.EncryptableSetting;
import software.wings.beans.ExecutionCredential;
import software.wings.beans.HostValidationResponse;
import software.wings.beans.SettingAttribute;
import software.wings.beans.WinRmConnectionAttributes;
import software.wings.beans.command.CommandExecutionContext;
import software.wings.beans.infrastructure.Host;
import software.wings.core.winrm.executors.WinRmSession;
import software.wings.core.winrm.executors.WinRmSessionConfig;
import software.wings.service.intfc.security.EncryptionService;

import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Singleton
@Slf4j
public class HostValidationServiceImpl implements HostValidationService {
  @Inject private EncryptionService encryptionService;
  @Inject private TimeLimiter timeLimiter;
  @Inject protected FeatureFlagService featureFlagService;

  @Override
  public List<HostValidationResponse> validateHost(List<String> hostNames, SettingAttribute connectionSetting,
      List<EncryptedDataDetail> encryptionDetails, ExecutionCredential executionCredential) {
    List<HostValidationResponse> hostValidationResponses = new ArrayList<>();

    encryptionService.decrypt((EncryptableSetting) connectionSetting.getValue(), encryptionDetails, false);
    try {
      timeLimiter.callWithTimeout(() -> {
        hostNames.forEach(hostName -> {
          HostValidationResponse response;
          if (connectionSetting.getValue() instanceof WinRmConnectionAttributes) {
            response = validateHostWinRm(hostName, (WinRmConnectionAttributes) connectionSetting.getValue());
          } else {
            response = validateHostSsh(hostName, connectionSetting, executionCredential);
          }
          hostValidationResponses.add(response);
        });
        return true;
      }, 1, TimeUnit.MINUTES, true);
    } catch (UncheckedTimeoutException ex) {
      log.warn("Host validation timed out", ex);
      // populate timeout error for rest of the hosts
      for (int idx = hostValidationResponses.size(); idx < hostNames.size(); idx++) {
        hostValidationResponses.add(HostValidationResponse.Builder.aHostValidationResponse()
                                        .withHostName(hostNames.get(idx))
                                        .withStatus(ExecutionStatus.FAILED.name())
                                        .withErrorCode(ErrorCode.REQUEST_TIMEOUT.name())
                                        .withErrorDescription(ErrorCode.REQUEST_TIMEOUT.getDescription())
                                        .build());
      }
    } catch (Exception ex) {
      log.warn("Host validation failed", ex);
      // populate error for rest of the hosts
      for (int idx = hostValidationResponses.size(); idx < hostNames.size(); idx++) {
        hostValidationResponses.add(HostValidationResponse.Builder.aHostValidationResponse()
                                        .withHostName(hostNames.get(idx))
                                        .withStatus(ExecutionStatus.FAILED.name())
                                        .withErrorCode(ErrorCode.UNKNOWN_ERROR.name())
                                        .withErrorDescription(ErrorCode.UNKNOWN_ERROR.getDescription())
                                        .build());
      }
    }
    return hostValidationResponses;
  }

  private HostValidationResponse validateHostSsh(
      String hostName, SettingAttribute connectionSetting, ExecutionCredential executionCredential) {
    CommandExecutionContext commandExecutionContext =
        aCommandExecutionContext(
            featureFlagService.isEnabledForAllAccounts(FeatureName.SSH_HOST_VALIDATION_STRIP_SECRETS),
            featureFlagService.isEnabledForAllAccounts(FeatureName.SSH_HOST_VALIDATION_STRIP_SECRETS_AB))
            .hostConnectionAttributes(connectionSetting)
            .executionCredential(executionCredential)
            .host(Host.Builder.aHost().withHostName(hostName).withPublicDns(hostName).build())
            .build();
    SshSessionConfig sshSessionConfig = createSshSessionConfig("HOST_CONNECTION_TEST", commandExecutionContext);

    HostValidationResponse response = HostValidationResponse.Builder.aHostValidationResponse()
                                          .withHostName(hostName)
                                          .withStatus(ExecutionStatus.SUCCESS.name())
                                          .build();
    try {
      Session sshSession = SshSessionFactory.getSSHSession(sshSessionConfig);
      sshSession.disconnect();
    } catch (JSchException jschEx) {
      ErrorCode errorCode = normalizeError(jschEx);
      response.setStatus(ExecutionStatus.FAILED.name());
      response.setErrorCode(errorCode.name());
      response.setErrorDescription(errorCode.getDescription());
      log.error("Failed to validate Host: ", jschEx);
    }
    return response;
  }

  private HostValidationResponse validateHostWinRm(String hostName, WinRmConnectionAttributes connectionAttributes) {
    HostValidationResponse response = HostValidationResponse.Builder.aHostValidationResponse()
                                          .withHostName(hostName)
                                          .withStatus(ExecutionStatus.SUCCESS.name())
                                          .build();
    WinRmSessionConfig config;

    config = WinRmSessionConfig.builder()
                 .hostname(hostName)
                 .commandUnitName("HOST_CONNECTION_TEST")
                 .domain(connectionAttributes.getDomain())
                 .username(connectionAttributes.getUsername())
                 .password(connectionAttributes.isUseKeyTab() ? StringUtils.EMPTY
                                                              : String.valueOf(connectionAttributes.getPassword()))
                 .authenticationScheme(connectionAttributes.getAuthenticationScheme())
                 .port(connectionAttributes.getPort())
                 .skipCertChecks(connectionAttributes.isSkipCertChecks())
                 .useSSL(connectionAttributes.isUseSSL())
                 .useKeyTab(connectionAttributes.isUseKeyTab())
                 .keyTabFilePath(connectionAttributes.getKeyTabFilePath())
                 .workingDirectory(WINDOWS_HOME_DIR)
                 .environment(Collections.emptyMap())
                 .useNoProfile(connectionAttributes.isUseNoProfile())
                 .build();

    try (WinRmSession ignore = new WinRmSession(config, new NoopExecutionCallback())) {
      noop();
    } catch (Exception e) {
      ResponseMessage details = buildErrorDetailsFromWinRmClientException(e);
      response.setStatus(ExecutionStatus.FAILED.name());
      response.setErrorCode(details.getCode().name());
      response.setErrorDescription(details.getMessage());
    }
    return response;
  }
}
