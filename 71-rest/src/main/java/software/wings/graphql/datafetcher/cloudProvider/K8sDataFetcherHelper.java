package software.wings.graphql.datafetcher.cloudProvider;

import static software.wings.graphql.schema.type.cloudProvider.k8s.QLClusterDetailsType.MANUAL_CLUSTER_DETAILS;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.harness.exception.InvalidRequestException;
import io.harness.utils.RequestField;
import software.wings.beans.KubernetesClusterAuthType;
import software.wings.beans.KubernetesClusterConfig;
import software.wings.beans.KubernetesClusterConfig.KubernetesClusterConfigBuilder;
import software.wings.beans.SettingAttribute;
import software.wings.graphql.datafetcher.secrets.UsageScopeController;
import software.wings.graphql.schema.mutation.cloudProvider.k8s.QLInheritClusterDetails;
import software.wings.graphql.schema.mutation.cloudProvider.k8s.QLK8sCloudProviderInput;
import software.wings.graphql.schema.mutation.cloudProvider.k8s.QLUpdateInheritClusterDetails;
import software.wings.graphql.schema.mutation.cloudProvider.k8s.QLUpdateK8sCloudProviderInput;
import software.wings.graphql.schema.type.secrets.QLUsageScope;

@Singleton
public class K8sDataFetcherHelper {
  @Inject private UsageScopeController usageScopeController;

  public SettingAttribute toSettingAttribute(QLK8sCloudProviderInput input, String accountId) {
    KubernetesClusterConfigBuilder configBuilder = KubernetesClusterConfig.builder().accountId(accountId);

    if (input.getSkipValidation().isPresent()) {
      input.getSkipValidation().getValue().ifPresent(configBuilder::skipValidation);
    }

    SettingAttribute.Builder settingAttributeBuilder =
        SettingAttribute.Builder.aSettingAttribute().withAccountId(accountId).withCategory(
            SettingAttribute.SettingCategory.SETTING);

    if (input.getClusterDetailsType().isPresent()) {
      switch (input.getClusterDetailsType().getValue().orElseThrow(
          () -> new InvalidRequestException("No cluster details type provided"))) {
        case INHERIT_CLUSTER_DETAILS:
          configBuilder.useKubernetesDelegate(true);
          if (input.getInheritClusterDetails().isPresent()) {
            input.getInheritClusterDetails().getValue().ifPresent(
                clusterDetails -> clusterDetails.getDelegateName().getValue().ifPresent(configBuilder::delegateName));
            QLInheritClusterDetails inheritClusterDetails = input.getInheritClusterDetails().getValue().orElse(null);
            RequestField<QLUsageScope> usageRestrictions = inheritClusterDetails.getUsageScope();
            if (usageRestrictions != null && usageRestrictions.isPresent()) {
              settingAttributeBuilder.withUsageRestrictions(
                  usageScopeController.populateUsageRestrictions(usageRestrictions.getValue().orElse(null), accountId));
            }
          }
          break;
        case MANUAL_CLUSTER_DETAILS:
          configBuilder.useKubernetesDelegate(false);
          if (input.getManualClusterDetails().isPresent()) {
            input.getManualClusterDetails().getValue().ifPresent(clusterDetails -> {
              clusterDetails.getMasterUrl().getValue().ifPresent(configBuilder::masterUrl);

              clusterDetails.getType().getValue().ifPresent(type -> {
                switch (type) {
                  case USERNAME_AND_PASSWORD:
                    configBuilder.authType(KubernetesClusterAuthType.USER_PASSWORD);
                    clusterDetails.getUsernameAndPassword().getValue().ifPresent(auth -> {
                      auth.getUserName().getValue().ifPresent(configBuilder::username);
                      auth.getPasswordSecretId().getValue().ifPresent(configBuilder::encryptedPassword);
                    });
                    break;
                  case SERVICE_ACCOUNT_TOKEN:
                    configBuilder.authType(KubernetesClusterAuthType.SERVICE_ACCOUNT);
                    clusterDetails.getServiceAccountToken().getValue().ifPresent(auth
                        -> auth.getServiceAccountTokenSecretId().getValue().ifPresent(
                            configBuilder::encryptedServiceAccountToken));
                    break;
                  case OIDC_TOKEN:
                    configBuilder.authType(KubernetesClusterAuthType.OIDC);
                    clusterDetails.getOidcToken().getValue().ifPresent(auth -> {
                      auth.getIdentityProviderUrl().getValue().ifPresent(configBuilder::oidcIdentityProviderUrl);
                      auth.getUserName().getValue().ifPresent(configBuilder::oidcUsername);

                      auth.getPasswordSecretId().getValue().ifPresent(configBuilder::encryptedOidcPassword);
                      auth.getClientIdSecretId().getValue().ifPresent(configBuilder::encryptedOidcClientId);
                      auth.getClientSecretSecretId().getValue().ifPresent(configBuilder::encryptedOidcSecret);

                      auth.getScopes().getValue().ifPresent(configBuilder::oidcScopes);
                    });
                    break;
                  case NONE:
                    configBuilder.authType(KubernetesClusterAuthType.NONE);

                    clusterDetails.getNone().getValue().ifPresent(auth -> {
                      auth.getUserName().getValue().ifPresent(configBuilder::username);
                      auth.getPasswordSecretId().getValue().ifPresent(configBuilder::encryptedPassword);

                      auth.getCaCertificateSecretId().getValue().ifPresent(configBuilder::encryptedCaCert);
                      auth.getClientCertificateSecretId().getValue().ifPresent(configBuilder::encryptedClientCert);
                      auth.getClientKeySecretId().getValue().ifPresent(configBuilder::encryptedClientKey);
                      auth.getClientKeyPassphraseSecretId().getValue().ifPresent(
                          configBuilder::encryptedClientKeyPassphrase);

                      auth.getClientKeyAlgorithm().getValue().ifPresent(configBuilder::clientKeyAlgo);

                      auth.getServiceAccountTokenSecretId().getValue().ifPresent(
                          configBuilder::encryptedServiceAccountToken);
                      RequestField<QLUsageScope> usageRestrictions = auth.getUsageScope();
                      if (usageRestrictions != null && usageRestrictions.isPresent()) {
                        checkIfUsageScopeCanBeCreatedOrUpdated(configBuilder.build());
                        settingAttributeBuilder.withUsageRestrictions(usageScopeController.populateUsageRestrictions(
                            usageRestrictions.getValue().orElse(null), accountId));
                      }
                    });
                    break;
                  default:
                    throw new InvalidRequestException("Invalid manual cluster details type");
                }
              });
            });
          }
          break;
        default:
          throw new InvalidRequestException("Invalid cluster details type");
      }
    }

    settingAttributeBuilder.withValue(configBuilder.build());

    if (input.getName().isPresent()) {
      input.getName().getValue().ifPresent(settingAttributeBuilder::withName);
    }

    return settingAttributeBuilder.build();
  }

  public void updateSettingAttribute(
      SettingAttribute settingAttribute, QLUpdateK8sCloudProviderInput input, String accountId) {
    KubernetesClusterConfig config = (KubernetesClusterConfig) settingAttribute.getValue();

    if (input.getSkipValidation().isPresent()) {
      input.getSkipValidation().getValue().ifPresent(config::setSkipValidation);
    }

    if (input.getClusterDetailsType().isPresent()) {
      switch (input.getClusterDetailsType().getValue().orElseThrow(
          () -> new InvalidRequestException("No cluster details type provided"))) {
        case INHERIT_CLUSTER_DETAILS:
          config.setUseKubernetesDelegate(true);
          if (input.getInheritClusterDetails().isPresent()) {
            input.getInheritClusterDetails().getValue().ifPresent(
                clusterDetails -> clusterDetails.getDelegateName().getValue().ifPresent(config::setDelegateName));
            QLUpdateInheritClusterDetails inheritClusterDetails =
                input.getInheritClusterDetails().getValue().orElseThrow(
                    () -> new InvalidRequestException(" No Inherit cluster details supplied"));
            RequestField<QLUsageScope> usageRestrictions = inheritClusterDetails.getUsageScope();
            if (usageRestrictions != null && usageRestrictions.isPresent()) {
              settingAttribute.setUsageRestrictions(
                  usageScopeController.populateUsageRestrictions(usageRestrictions.getValue().orElse(null), accountId));
            }
          }
          break;
        case MANUAL_CLUSTER_DETAILS:
          config.setUseKubernetesDelegate(false);
          config.setDelegateName(null);
          if (input.getManualClusterDetails().isPresent()) {
            input.getManualClusterDetails().getValue().ifPresent(clusterDetails -> {
              clusterDetails.getMasterUrl().getValue().ifPresent(config::setMasterUrl);

              clusterDetails.getType().getValue().ifPresent(type -> {
                switch (type) {
                  case USERNAME_AND_PASSWORD:
                    config.setAuthType(KubernetesClusterAuthType.USER_PASSWORD);
                    clusterDetails.getUsernameAndPassword().getValue().ifPresent(auth -> {
                      auth.getUserName().getValue().ifPresent(config::setUsername);
                      auth.getPasswordSecretId().getValue().ifPresent(config::setEncryptedPassword);
                    });
                    break;
                  case SERVICE_ACCOUNT_TOKEN:
                    config.setAuthType(KubernetesClusterAuthType.SERVICE_ACCOUNT);
                    clusterDetails.getServiceAccountToken().getValue().ifPresent(auth
                        -> auth.getServiceAccountTokenSecretId().getValue().ifPresent(
                            config::setEncryptedServiceAccountToken));
                    break;
                  case OIDC_TOKEN:
                    config.setAuthType(KubernetesClusterAuthType.OIDC);
                    clusterDetails.getOidcToken().getValue().ifPresent(auth -> {
                      auth.getIdentityProviderUrl().getValue().ifPresent(config::setOidcIdentityProviderUrl);
                      auth.getUserName().getValue().ifPresent(config::setOidcUsername);

                      auth.getPasswordSecretId().getValue().ifPresent(config::setEncryptedOidcPassword);
                      auth.getClientIdSecretId().getValue().ifPresent(config::setEncryptedOidcClientId);
                      auth.getClientSecretSecretId().getValue().ifPresent(config::setEncryptedOidcSecret);

                      auth.getScopes().getValue().ifPresent(config::setOidcScopes);
                    });
                    break;
                  case NONE:
                    config.setAuthType(KubernetesClusterAuthType.NONE);
                    clusterDetails.getNone().getValue().ifPresent(auth -> {
                      auth.getUserName().getValue().ifPresent(config::setUsername);
                      auth.getPasswordSecretId().getValue().ifPresent(config::setEncryptedPassword);

                      auth.getCaCertificateSecretId().getValue().ifPresent(config::setEncryptedCaCert);
                      auth.getClientCertificateSecretId().getValue().ifPresent(config::setEncryptedClientCert);
                      auth.getClientKeySecretId().getValue().ifPresent(config::setEncryptedClientKey);
                      auth.getClientKeyPassphraseSecretId().getValue().ifPresent(
                          config::setEncryptedClientKeyPassphrase);

                      auth.getClientKeyAlgorithm().getValue().ifPresent(config::setClientKeyAlgo);

                      auth.getServiceAccountTokenSecretId().getValue().ifPresent(
                          config::setEncryptedServiceAccountToken);
                      RequestField<QLUsageScope> usageRestrictions = auth.getUsageScope();
                      if (usageRestrictions != null && usageRestrictions.isPresent()) {
                        checkIfUsageScopeCanBeCreatedOrUpdated(config);
                        settingAttribute.setUsageRestrictions(usageScopeController.populateUsageRestrictions(
                            usageRestrictions.getValue().orElse(null), accountId));
                      }
                    });
                    break;
                  default:
                    throw new InvalidRequestException("Invalid manual cluster details type");
                }
              });
            });
          }
          break;
        default:
          throw new InvalidRequestException("Invalid cluster details type");
      }
    }

    settingAttribute.setValue(config);

    if (input.getName().isPresent()) {
      input.getName().getValue().ifPresent(settingAttribute::setName);
    }
  }

  private void checkIfUsageScopeCanBeCreatedOrUpdated(KubernetesClusterConfig config) {
    if (theCloudProviderUsesSecretId(config)) {
      throw new InvalidRequestException(
          "The usage scope should not be provided, when a secretId is provided in the api the scope will be automatically inherited from the secret");
    }
  }

  private boolean theCloudProviderUsesSecretId(KubernetesClusterConfig finalConfig) {
    if (finalConfig.getEncryptedPassword() != null) {
      return true;
    }

    if (finalConfig.getEncryptedCaCert() != null) {
      return true;
    }

    if (finalConfig.getEncryptedCaCert() != null) {
      return true;
    }

    if (finalConfig.getEncryptedCaCert() != null) {
      return true;
    }

    if (finalConfig.getEncryptedClientKeyPassphrase() != null) {
      return true;
    }

    if (finalConfig.getEncryptedServiceAccountToken() != null) {
      return true;
    }

    return false;
  }
}
