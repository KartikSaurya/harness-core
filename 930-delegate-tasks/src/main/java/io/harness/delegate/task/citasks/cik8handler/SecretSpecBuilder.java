package io.harness.delegate.task.citasks.cik8handler;

import static io.harness.data.encoding.EncodingUtils.encodeBase64;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.delegate.beans.ci.pod.SecretParams.Type.FILE;
import static io.harness.delegate.beans.ci.pod.SecretParams.Type.TEXT;
import static io.harness.delegate.beans.connector.ConnectorType.BITBUCKET;
import static io.harness.delegate.beans.connector.ConnectorType.CODECOMMIT;
import static io.harness.delegate.beans.connector.ConnectorType.GIT;
import static io.harness.delegate.beans.connector.ConnectorType.GITHUB;
import static io.harness.delegate.beans.connector.ConnectorType.GITLAB;
import static io.harness.govern.Switch.unhandled;
import static io.harness.utils.FieldWithPlainTextOrSecretValueHelper.getSecretAsStringFromPlainTextOrSecretRef;

import static java.lang.String.format;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.ci.pod.ConnectorDetails;
import io.harness.delegate.beans.ci.pod.SSHKeyDetails;
import io.harness.delegate.beans.ci.pod.SecretParams;
import io.harness.delegate.beans.ci.pod.SecretVariableDTO;
import io.harness.delegate.beans.ci.pod.SecretVariableDetails;
import io.harness.delegate.beans.connector.ConnectorType;
import io.harness.delegate.beans.connector.scm.GitAuthType;
import io.harness.delegate.beans.connector.scm.awscodecommit.AwsCodeCommitAuthType;
import io.harness.delegate.beans.connector.scm.awscodecommit.AwsCodeCommitConnectorDTO;
import io.harness.delegate.beans.connector.scm.awscodecommit.AwsCodeCommitHttpsAuthType;
import io.harness.delegate.beans.connector.scm.awscodecommit.AwsCodeCommitHttpsCredentialsDTO;
import io.harness.delegate.beans.connector.scm.awscodecommit.AwsCodeCommitSecretKeyAccessKeyDTO;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketConnectorDTO;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketHttpAuthenticationType;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketHttpCredentialsDTO;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketUsernamePasswordDTO;
import io.harness.delegate.beans.connector.scm.genericgitconnector.GitConfigDTO;
import io.harness.delegate.beans.connector.scm.genericgitconnector.GitHTTPAuthenticationDTO;
import io.harness.delegate.beans.connector.scm.github.GithubConnectorDTO;
import io.harness.delegate.beans.connector.scm.github.GithubHttpAuthenticationType;
import io.harness.delegate.beans.connector.scm.github.GithubHttpCredentialsDTO;
import io.harness.delegate.beans.connector.scm.github.GithubUsernamePassword;
import io.harness.delegate.beans.connector.scm.github.GithubUsernameToken;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabConnectorDTO;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabHttpAuthenticationType;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabHttpCredentialsDTO;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabUsernamePasswordDTO;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabUsernameTokenDTO;
import io.harness.delegate.task.citasks.cik8handler.helper.ConnectorEnvVariablesHelper;
import io.harness.delegate.task.gitapi.client.impl.GithubApiClient;
import io.harness.encryption.SecretRefData;
import io.harness.exception.ngexception.CIStageExecutionException;
import io.harness.security.encryption.SecretDecryptionService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class to create spec for image registry and GIT secrets. Generated spec can be used for creation of secrets on
 * a K8 cluster.
 */

@Slf4j
@Singleton
@OwnedBy(HarnessTeam.CI)
public class SecretSpecBuilder {
  public static final String DOCKER_REGISTRY_SECRET_TYPE = "kubernetes.io/dockercfg";
  public static final String SECRET_KEY = "secret_key";
  public static final String SECRET = "secret";
  public static final String OPAQUE_SECRET_TYPE = "opaque";
  private static final String DRONE_NETRC_PASSWORD = "DRONE_NETRC_PASSWORD";
  private static final String DRONE_NETRC_USERNAME = "DRONE_NETRC_USERNAME";
  private static final String DRONE_AWS_ACCESS_KEY = "DRONE_AWS_ACCESS_KEY";
  private static final String DRONE_AWS_SECRET_KEY = "DRONE_AWS_SECRET_KEY";

  private static final String DRONE_SSH_KEY = "DRONE_SSH_KEY";

  @Inject private ConnectorEnvVariablesHelper connectorEnvVariablesHelper;
  @Inject private SecretDecryptionService secretDecryptionService;
  @Inject private ImageSecretBuilder imageSecretBuilder;
  @Inject private GithubApiClient githubApiClient;

  public Map<String, SecretParams> decryptCustomSecretVariables(List<SecretVariableDetails> secretVariableDetails) {
    Map<String, SecretParams> data = new HashMap<>();
    if (isNotEmpty(secretVariableDetails)) {
      for (SecretVariableDetails secretVariableDetail : secretVariableDetails) {
        log.info("Decrypting custom variable name:[{}], type:[{}], secretRef:[{}]",
            secretVariableDetail.getSecretVariableDTO().getName(),
            secretVariableDetail.getSecretVariableDTO().getType(),
            secretVariableDetail.getSecretVariableDTO().getSecret().toSecretRefStringValue());
        SecretVariableDTO secretVariableDTO = (SecretVariableDTO) secretDecryptionService.decrypt(
            secretVariableDetail.getSecretVariableDTO(), secretVariableDetail.getEncryptedDataDetailList());

        log.info("Decrypted custom variable name:[{}], type:[{}], secretRef:[{}]",
            secretVariableDetail.getSecretVariableDTO().getName(),
            secretVariableDetail.getSecretVariableDTO().getType(),
            secretVariableDetail.getSecretVariableDTO().getSecret().toSecretRefStringValue());
        switch (secretVariableDTO.getType()) {
          case FILE:
            data.put(secretVariableDTO.getName(),
                SecretParams.builder()
                    .secretKey(SECRET_KEY + secretVariableDTO.getName())
                    .type(FILE)
                    .value(encodeBase64(secretVariableDTO.getSecret().getDecryptedValue()))
                    .build());
            break;
          case TEXT:
            data.put(secretVariableDTO.getName(),
                SecretParams.builder()
                    .secretKey(SECRET_KEY + secretVariableDTO.getName())
                    .type(TEXT)
                    .value(encodeBase64(secretVariableDTO.getSecret().getDecryptedValue()))
                    .build());
            break;
          default:
            unhandled(secretVariableDTO.getType());
        }
      }
    }

    return data;
  }

  public Map<String, SecretParams> decryptConnectorSecretVariables(Map<String, ConnectorDetails> connectorDetailsMap) {
    Map<String, SecretParams> secretData = new HashMap<>();
    if (isEmpty(connectorDetailsMap)) {
      return secretData;
    }

    for (Map.Entry<String, ConnectorDetails> connectorDetailsEntry : connectorDetailsMap.entrySet()) {
      ConnectorDetails connectorDetails = connectorDetailsEntry.getValue();

      log.info("Decrypting connector id:[{}], type:[{}]", connectorDetails.getIdentifier(),
          connectorDetails.getConnectorType());
      if (connectorDetails.getConnectorType() == ConnectorType.DOCKER) {
        secretData.putAll(connectorEnvVariablesHelper.getDockerSecretVariables(connectorDetails));
      } else if (connectorDetails.getConnectorType() == ConnectorType.AWS) {
        secretData.putAll(connectorEnvVariablesHelper.getAwsSecretVariables(connectorDetails));
      } else if (connectorDetails.getConnectorType() == ConnectorType.GCP) {
        secretData.putAll(connectorEnvVariablesHelper.getGcpSecretVariables(connectorDetails));
      } else if (connectorDetails.getConnectorType() == ConnectorType.ARTIFACTORY) {
        secretData.putAll(connectorEnvVariablesHelper.getArtifactorySecretVariables(connectorDetails));
      }
      log.info("Decrypted connector id:[{}], type:[{}]", connectorDetails.getIdentifier(),
          connectorDetails.getConnectorType());
    }
    return secretData;
  }

  public Map<String, SecretParams> decryptGitSecretVariables(ConnectorDetails gitConnector) {
    if (gitConnector == null) {
      return new HashMap<>();
    }

    if (gitConnector.getConnectorType() == ConnectorType.GITHUB) {
      GithubConnectorDTO gitConfigDTO = (GithubConnectorDTO) gitConnector.getConnectorConfig();
      return retrieveGitHubSecretParams(gitConfigDTO, gitConnector);
    } else if (gitConnector.getConnectorType() == GITLAB) {
      GitlabConnectorDTO gitConfigDTO = (GitlabConnectorDTO) gitConnector.getConnectorConfig();
      return retrieveGitlabSecretParams(gitConfigDTO, gitConnector);
    } else if (gitConnector.getConnectorType() == BITBUCKET) {
      BitbucketConnectorDTO gitConfigDTO = (BitbucketConnectorDTO) gitConnector.getConnectorConfig();
      return retrieveBitbucketSecretParams(gitConfigDTO, gitConnector);
    } else if (gitConnector.getConnectorType() == CODECOMMIT) {
      AwsCodeCommitConnectorDTO gitConfigDTO = (AwsCodeCommitConnectorDTO) gitConnector.getConnectorConfig();
      return retrieveAwsCodeCommitSecretParams(gitConfigDTO, gitConnector);
    } else if (gitConnector.getConnectorType() == GIT) {
      GitConfigDTO gitConfigDTO = (GitConfigDTO) gitConnector.getConnectorConfig();
      return retrieveGitSecretParams(gitConfigDTO, gitConnector);
    } else {
      throw new CIStageExecutionException("Unsupported git connector type" + gitConnector.getConnectorType());
    }
  }

  private Map<String, SecretParams> retrieveGitSecretParams(GitConfigDTO gitConfigDTO, ConnectorDetails gitConnector) {
    Map<String, SecretParams> secretData = new HashMap<>();
    if (gitConnector == null) {
      return secretData;
    }

    log.info(
        "Decrypting git connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
    if (gitConfigDTO.getGitAuthType() == GitAuthType.HTTP) {
      secretDecryptionService.decrypt(gitConfigDTO.getGitAuth(), gitConnector.getEncryptedDataDetails());
      GitHTTPAuthenticationDTO gitHTTPAuthenticationDTO = (GitHTTPAuthenticationDTO) gitConfigDTO.getGitAuth();

      String key = DRONE_NETRC_PASSWORD;
      secretData.put(key,
          SecretParams.builder()
              .secretKey(key)
              .value(encodeBase64(new String(gitHTTPAuthenticationDTO.getPasswordRef().getDecryptedValue())))
              .type(TEXT)
              .build());
    } else if (gitConfigDTO.getGitAuthType() == GitAuthType.SSH) {
      SSHKeyDetails sshKeyDetails = gitConnector.getSshKeyDetails();
      secretDecryptionService.decrypt(sshKeyDetails.getSshKeyReference(), sshKeyDetails.getEncryptedDataDetails());
      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      SecretRefData key = sshKeyDetails.getSshKeyReference().getKey();
      if (key == null || isEmpty(key.getDecryptedValue())) {
        throw new CIStageExecutionException("Git connector should have not empty sshKey");
      }
      char[] sshKey = key.getDecryptedValue();
      secretData.put(DRONE_SSH_KEY,
          SecretParams.builder().secretKey(DRONE_SSH_KEY).value(encodeBase64(sshKey)).type(TEXT).build());
    }
    return secretData;
  }

  private Map<String, SecretParams> retrieveAwsCodeCommitSecretParams(
      AwsCodeCommitConnectorDTO gitConfigDTO, ConnectorDetails gitConnector) {
    Map<String, SecretParams> secretData = new HashMap<>();

    if (gitConfigDTO.getAuthentication().getAuthType() == AwsCodeCommitAuthType.HTTPS) {
      log.info("Decrypting AwsCodeCommit connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());

      gitConfigDTO.getDecryptableEntities().forEach(decryptableEntity
          -> secretDecryptionService.decrypt(decryptableEntity, gitConnector.getEncryptedDataDetails()));

      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      AwsCodeCommitHttpsCredentialsDTO credentials =
          (AwsCodeCommitHttpsCredentialsDTO) gitConfigDTO.getAuthentication().getCredentials();
      if (credentials.getType() == AwsCodeCommitHttpsAuthType.ACCESS_KEY_AND_SECRET_KEY) {
        AwsCodeCommitSecretKeyAccessKeyDTO secretKeyAccessKeyDTO =
            (AwsCodeCommitSecretKeyAccessKeyDTO) credentials.getHttpCredentialsSpec();

        String accessKey = getSecretAsStringFromPlainTextOrSecretRef(
            secretKeyAccessKeyDTO.getAccessKey(), secretKeyAccessKeyDTO.getAccessKeyRef());
        if (isEmpty(accessKey)) {
          throw new CIStageExecutionException(
              "AwsCodeCommit connector should have not empty accessKey and accessKeyRef");
        }
        secretData.put(DRONE_AWS_ACCESS_KEY,
            SecretParams.builder().secretKey(DRONE_AWS_ACCESS_KEY).value(encodeBase64(accessKey)).type(TEXT).build());

        if (secretKeyAccessKeyDTO.getSecretKeyRef() == null) {
          throw new CIStageExecutionException("AwsCodeCommit connector should have not empty secretKeyRef");
        }
        String secretKey = String.valueOf(secretKeyAccessKeyDTO.getSecretKeyRef().getDecryptedValue());
        if (isEmpty(secretKey)) {
          throw new CIStageExecutionException("AwsCodeCommit connector should have not empty secretKeyRef");
        }
        secretData.put(DRONE_AWS_SECRET_KEY,
            SecretParams.builder().secretKey(DRONE_AWS_SECRET_KEY).value(encodeBase64(secretKey)).type(TEXT).build());
      }
    } else {
      throw new CIStageExecutionException(
          "Unsupported github connector auth" + gitConfigDTO.getAuthentication().getAuthType());
    }
    return secretData;
  }

  public static String getSecretName(String podName) {
    return podName + "-" + SECRET;
  }

  private Map<String, SecretParams> retrieveGitHubSecretParams(
      GithubConnectorDTO gitConfigDTO, ConnectorDetails gitConnector) {
    Map<String, SecretParams> secretData = new HashMap<>();

    if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.HTTP) {
      GithubHttpCredentialsDTO gitHTTPAuthenticationDTO =
          (GithubHttpCredentialsDTO) gitConfigDTO.getAuthentication().getCredentials();

      log.info("Decrypting GitHub connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());

      secretDecryptionService.decrypt(
          gitHTTPAuthenticationDTO.getHttpCredentialsSpec(), gitConnector.getEncryptedDataDetails());

      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      if (gitHTTPAuthenticationDTO.getType() == GithubHttpAuthenticationType.USERNAME_AND_PASSWORD) {
        GithubUsernamePassword githubUsernamePassword =
            (GithubUsernamePassword) gitHTTPAuthenticationDTO.getHttpCredentialsSpec();

        String username = getSecretAsStringFromPlainTextOrSecretRef(
            githubUsernamePassword.getUsername(), githubUsernamePassword.getUsernameRef());
        if (isEmpty(username)) {
          throw new CIStageExecutionException("Github connector should have not empty username");
        }
        secretData.put(DRONE_NETRC_USERNAME,
            SecretParams.builder().secretKey(DRONE_NETRC_USERNAME).value(encodeBase64(username)).type(TEXT).build());

        if (githubUsernamePassword.getPasswordRef() == null) {
          throw new CIStageExecutionException("Github connector should have not empty passwordRef");
        }
        String password = String.valueOf(githubUsernamePassword.getPasswordRef().getDecryptedValue());
        if (isEmpty(password)) {
          throw new CIStageExecutionException(
              "Unsupported github connector auth" + gitConfigDTO.getAuthentication().getAuthType());
        }
        secretData.put(DRONE_NETRC_PASSWORD,
            SecretParams.builder().secretKey(DRONE_NETRC_PASSWORD).value(encodeBase64(password)).type(TEXT).build());

      } else if (gitHTTPAuthenticationDTO.getType() == GithubHttpAuthenticationType.USERNAME_AND_TOKEN) {
        GithubUsernameToken githubUsernameToken =
            (GithubUsernameToken) gitHTTPAuthenticationDTO.getHttpCredentialsSpec();

        String username = getSecretAsStringFromPlainTextOrSecretRef(
            githubUsernameToken.getUsername(), githubUsernameToken.getUsernameRef());
        if (isEmpty(username)) {
          throw new CIStageExecutionException("Github connector should have not empty username");
        }
        secretData.put(DRONE_NETRC_USERNAME,
            SecretParams.builder().secretKey(DRONE_NETRC_USERNAME).value(encodeBase64(username)).type(TEXT).build());

        if (githubUsernameToken.getTokenRef() == null) {
          throw new CIStageExecutionException("Github connector should have not empty tokenRef");
        }
        String token = String.valueOf(githubUsernameToken.getTokenRef().getDecryptedValue());
        if (isEmpty(token)) {
          throw new CIStageExecutionException("Github connector should have not empty token");
        }
        secretData.put(DRONE_NETRC_PASSWORD,
            SecretParams.builder().secretKey(DRONE_NETRC_PASSWORD).value(encodeBase64(token)).type(TEXT).build());

      } else {
        throw new CIStageExecutionException(
            "Unsupported github connector auth type" + gitHTTPAuthenticationDTO.getType());
      }

    } else if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.SSH) {
      SSHKeyDetails sshKeyDetails = gitConnector.getSshKeyDetails();
      log.info("Decrypting GitHub connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());

      secretDecryptionService.decrypt(sshKeyDetails.getSshKeyReference(), sshKeyDetails.getEncryptedDataDetails());
      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      SecretRefData key = sshKeyDetails.getSshKeyReference().getKey();
      if (key == null || isEmpty(key.getDecryptedValue())) {
        throw new CIStageExecutionException("Github connector should have not empty sshKey");
      }
      char[] sshKey = key.getDecryptedValue();
      secretData.put(DRONE_SSH_KEY,
          SecretParams.builder().secretKey(DRONE_SSH_KEY).value(encodeBase64(sshKey)).type(TEXT).build());

    } else {
      throw new CIStageExecutionException(
          "Unsupported github connector auth" + gitConfigDTO.getAuthentication().getAuthType());
    }

    return secretData;
  }

  private Map<String, SecretParams> retrieveGitlabSecretParams(
      GitlabConnectorDTO gitConfigDTO, ConnectorDetails gitConnector) {
    Map<String, SecretParams> secretData = new HashMap<>();

    if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.HTTP) {
      GitlabHttpCredentialsDTO gitHTTPAuthenticationDTO =
          (GitlabHttpCredentialsDTO) gitConfigDTO.getAuthentication().getCredentials();

      log.info("Decrypting GitLab connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());
      secretDecryptionService.decrypt(
          gitHTTPAuthenticationDTO.getHttpCredentialsSpec(), gitConnector.getEncryptedDataDetails());
      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      if (gitHTTPAuthenticationDTO.getType() == GitlabHttpAuthenticationType.USERNAME_AND_PASSWORD) {
        GitlabUsernamePasswordDTO gitlabHttpCredentialsSpecDTO =
            (GitlabUsernamePasswordDTO) gitHTTPAuthenticationDTO.getHttpCredentialsSpec();

        String username = getSecretAsStringFromPlainTextOrSecretRef(
            gitlabHttpCredentialsSpecDTO.getUsername(), gitlabHttpCredentialsSpecDTO.getUsernameRef());
        if (isEmpty(username)) {
          throw new CIStageExecutionException("Gitlab connector should have not empty username");
        }
        secretData.put(DRONE_NETRC_USERNAME,
            SecretParams.builder().secretKey(DRONE_NETRC_USERNAME).value(encodeBase64(username)).type(TEXT).build());

        if (gitlabHttpCredentialsSpecDTO.getPasswordRef() == null) {
          throw new CIStageExecutionException("Gitlab connector should have not empty passwordRef");
        }
        String password = String.valueOf(gitlabHttpCredentialsSpecDTO.getPasswordRef().getDecryptedValue());
        if (isEmpty(password)) {
          throw new CIStageExecutionException(
              "Unsupported gitlab connector auth" + gitConfigDTO.getAuthentication().getAuthType());
        }
        secretData.put(DRONE_NETRC_PASSWORD,
            SecretParams.builder().secretKey(DRONE_NETRC_PASSWORD).value(encodeBase64(password)).type(TEXT).build());

      } else if (gitHTTPAuthenticationDTO.getType() == GitlabHttpAuthenticationType.USERNAME_AND_TOKEN) {
        GitlabUsernameTokenDTO gitlabUsernameTokenDTO =
            (GitlabUsernameTokenDTO) gitHTTPAuthenticationDTO.getHttpCredentialsSpec();

        String username = getSecretAsStringFromPlainTextOrSecretRef(
            gitlabUsernameTokenDTO.getUsername(), gitlabUsernameTokenDTO.getUsernameRef());
        if (isEmpty(username)) {
          throw new CIStageExecutionException("Gitlab connector should have not empty username");
        }
        secretData.put(DRONE_NETRC_USERNAME,
            SecretParams.builder().secretKey(DRONE_NETRC_USERNAME).value(encodeBase64(username)).type(TEXT).build());

        if (gitlabUsernameTokenDTO.getTokenRef() == null) {
          throw new CIStageExecutionException("Gitlab connector should have not empty tokenRef");
        }
        String token = String.valueOf(gitlabUsernameTokenDTO.getTokenRef().getDecryptedValue());
        if (isEmpty(token)) {
          throw new CIStageExecutionException("Gitlab connector should have not empty token");
        }
        secretData.put(DRONE_NETRC_PASSWORD,
            SecretParams.builder().secretKey(DRONE_NETRC_PASSWORD).value(encodeBase64(token)).type(TEXT).build());

      } else {
        throw new CIStageExecutionException(
            "Unsupported gitlab connector auth type" + gitHTTPAuthenticationDTO.getType());
      }
    } else if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.SSH) {
      SSHKeyDetails sshKeyDetails = gitConnector.getSshKeyDetails();
      log.info("Decrypting GitLab connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());
      secretDecryptionService.decrypt(sshKeyDetails.getSshKeyReference(), sshKeyDetails.getEncryptedDataDetails());
      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      SecretRefData key = sshKeyDetails.getSshKeyReference().getKey();
      if (key == null || isEmpty(key.getDecryptedValue())) {
        throw new CIStageExecutionException("Gitlab connector should have not empty sshKey");
      }
      char[] sshKey = key.getDecryptedValue();
      secretData.put(DRONE_SSH_KEY,
          SecretParams.builder().secretKey(DRONE_SSH_KEY).value(encodeBase64(sshKey)).type(TEXT).build());
    } else {
      throw new CIStageExecutionException(
          "Unsupported gitlab connector auth" + gitConfigDTO.getAuthentication().getAuthType());
    }

    return secretData;
  }

  private Map<String, SecretParams> retrieveBitbucketSecretParams(
      BitbucketConnectorDTO gitConfigDTO, ConnectorDetails gitConnector) {
    Map<String, SecretParams> secretData = new HashMap<>();

    if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.HTTP) {
      BitbucketHttpCredentialsDTO gitHTTPAuthenticationDTO =
          (BitbucketHttpCredentialsDTO) gitConfigDTO.getAuthentication().getCredentials();
      log.info("Decrypting Bitbucket connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());
      secretDecryptionService.decrypt(
          gitHTTPAuthenticationDTO.getHttpCredentialsSpec(), gitConnector.getEncryptedDataDetails());
      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      if (gitHTTPAuthenticationDTO.getType() == BitbucketHttpAuthenticationType.USERNAME_AND_PASSWORD) {
        BitbucketUsernamePasswordDTO bitbucketHttpCredentialsSpecDTO =
            (BitbucketUsernamePasswordDTO) gitHTTPAuthenticationDTO.getHttpCredentialsSpec();

        String username = getSecretAsStringFromPlainTextOrSecretRef(
            bitbucketHttpCredentialsSpecDTO.getUsername(), bitbucketHttpCredentialsSpecDTO.getUsernameRef());
        if (isEmpty(username)) {
          throw new CIStageExecutionException("Bitbucket connector should have not empty username");
        }
        secretData.put(DRONE_NETRC_USERNAME,
            SecretParams.builder().secretKey(DRONE_NETRC_USERNAME).value(encodeBase64(username)).type(TEXT).build());

        if (bitbucketHttpCredentialsSpecDTO.getPasswordRef() == null) {
          throw new CIStageExecutionException("Bitbucket connector should have not empty passwordRef");
        }
        String password = String.valueOf(bitbucketHttpCredentialsSpecDTO.getPasswordRef().getDecryptedValue());
        if (isEmpty(password)) {
          throw new CIStageExecutionException(
              "Unsupported bitbucket connector auth" + gitConfigDTO.getAuthentication().getAuthType());
        }
        secretData.put(DRONE_NETRC_PASSWORD,
            SecretParams.builder().secretKey(DRONE_NETRC_PASSWORD).value(encodeBase64(password)).type(TEXT).build());
      }
    } else if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.SSH) {
      SSHKeyDetails sshKeyDetails = gitConnector.getSshKeyDetails();
      log.info("Decrypting Bitbucket connector id:[{}], type:[{}]", gitConnector.getIdentifier(),
          gitConnector.getConnectorType());
      secretDecryptionService.decrypt(sshKeyDetails.getSshKeyReference(), sshKeyDetails.getEncryptedDataDetails());
      log.info("Decrypted connector id:[{}], type:[{}]", gitConnector.getIdentifier(), gitConnector.getConnectorType());
      SecretRefData key = sshKeyDetails.getSshKeyReference().getKey();
      if (key == null || isEmpty(key.getDecryptedValue())) {
        throw new CIStageExecutionException("Bitbucket connector should have not empty sshKey");
      }
      char[] sshKey = key.getDecryptedValue();
      secretData.put(DRONE_SSH_KEY,
          SecretParams.builder().secretKey(DRONE_SSH_KEY).value(encodeBase64(sshKey)).type(TEXT).build());
    } else {
      throw new CIStageExecutionException(
          "Unsupported bitbucket connector auth" + gitConfigDTO.getAuthentication().getAuthType());
    }

    return secretData;
  }

  public Map<String, SecretParams> fetchGithubAppToken(Map<String, ConnectorDetails> gitConnectors) {
    Map<String, SecretParams> secretParamsMap = new HashMap<>();
    if (isEmpty(gitConnectors)) {
      return secretParamsMap;
    }
    gitConnectors.forEach((key, gitConnector) -> {
      if (gitConnector.getConnectorType() != GITHUB) {
        throw new CIStageExecutionException(format(
            "Github token functor requires git hub connector, but %s was provided.", gitConnector.getConnectorType()));
      }
      String authToken = githubApiClient.retrieveAuthToken(gitConnector);
      secretParamsMap.put(key, SecretParams.builder().secretKey(key).type(TEXT).value(encodeBase64(authToken)).build());
    });
    return secretParamsMap;
  }

  public Map<String, SecretParams> createSecretParamsForPlainTextSecret(
      Map<String, String> envVarsWithSecretRef, String containerName) {
    Map<String, SecretParams> secretParamsMap = new HashMap<>();
    envVarsWithSecretRef.forEach((varName, varValue)
                                     -> secretParamsMap.put(varName,
                                         SecretParams.builder()
                                             .secretKey(SECRET_KEY + "_" + varName + "_" + containerName)
                                             .type(TEXT)
                                             .value(encodeBase64(varValue))
                                             .build()));
    return secretParamsMap;
  }
}
