/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.delegatetasks.validation.capabilitycheck;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.rule.OwnerRule.PRASHANT;

import static software.wings.beans.SettingAttribute.Builder.aSettingAttribute;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.ACTIVITY_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.executioncapability.CapabilityResponse;
import io.harness.rule.Owner;
import io.harness.shell.AccessType;
import io.harness.shell.SshSessionConfig;

import software.wings.WingsBaseTest;
import software.wings.beans.BastionConnectionAttributes;
import software.wings.beans.ExecutionCredential;
import software.wings.beans.HostConnectionAttributes;
import software.wings.beans.SSHExecutionCredential;
import software.wings.beans.SettingAttribute;
import software.wings.beans.infrastructure.Host;
import software.wings.delegatetasks.validation.capabilities.BasicValidationInfo;
import software.wings.delegatetasks.validation.capabilities.SSHHostValidationCapability;
import software.wings.service.intfc.security.EncryptionService;
import software.wings.utils.WingsTestConstants;

import com.jcraft.jsch.JSchException;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

@TargetModule(HarnessModule._930_DELEGATE_TASKS)
@OwnedBy(CDP)
public class SSHHostValidationCapabilityCheckTest extends WingsBaseTest {
  @Mock private EncryptionService encryptionService;
  @Spy @InjectMocks private SSHHostValidationCapabilityCheck sshHostValidationCapabilityCheck;

  private Host host = Host.Builder.aHost().withPublicDns(WingsTestConstants.PUBLIC_DNS).build();
  private SettingAttribute hostConnectionAttributes =
      aSettingAttribute()
          .withValue(HostConnectionAttributes.Builder.aHostConnectionAttributes()
                         .withAccessType(AccessType.USER_PASSWORD)
                         .withAccountId(WingsTestConstants.ACCOUNT_ID)
                         .build())
          .build();

  private SettingAttribute bastionConnectionAttributes =
      aSettingAttribute()
          .withValue(BastionConnectionAttributes.Builder.aBastionConnectionAttributes()
                         .withAccessType(AccessType.USER_PASSWORD)
                         .withAccountId(WingsTestConstants.ACCOUNT_ID)
                         .build())
          .build();

  private final SSHHostValidationCapability validationCapability =
      SSHHostValidationCapability.builder()
          .validationInfo(BasicValidationInfo.builder()
                              .accountId(ACCOUNT_ID)
                              .appId(APP_ID)
                              .activityId(ACTIVITY_ID)
                              .executeOnDelegate(false)
                              .publicDns(host.getPublicDns())
                              .build())
          .hostConnectionAttributes(hostConnectionAttributes)
          .bastionConnectionAttributes(bastionConnectionAttributes)
          .hostConnectionCredentials(new ArrayList<>())
          .bastionConnectionCredentials(new ArrayList<>())
          .sshExecutionCredential(SSHExecutionCredential.Builder.aSSHExecutionCredential()
                                      .withExecutionType(ExecutionCredential.ExecutionType.SSH)
                                      .build())
          .build();

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldPerformCapabilityCheck() throws JSchException {
    doNothing().when(sshHostValidationCapabilityCheck).performTest(any(SshSessionConfig.class));
    CapabilityResponse capabilityResponse =
        sshHostValidationCapabilityCheck.performCapabilityCheck(validationCapability);
    assertThat(capabilityResponse).isNotNull();
    assertThat(capabilityResponse.isValidated()).isTrue();
  }

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldPerformCapabilityCheckExecuteOnDelegate() {
    SSHHostValidationCapability validationCapability = SSHHostValidationCapability.builder()
                                                           .validationInfo(BasicValidationInfo.builder()
                                                                               .accountId(ACCOUNT_ID)
                                                                               .appId(APP_ID)
                                                                               .activityId(ACTIVITY_ID)
                                                                               .executeOnDelegate(true)
                                                                               .publicDns(host.getPublicDns())
                                                                               .build())
                                                           .build();
    CapabilityResponse capabilityResponse =
        sshHostValidationCapabilityCheck.performCapabilityCheck(validationCapability);
    assertThat(capabilityResponse).isNotNull();
    assertThat(capabilityResponse.isValidated()).isTrue();
  }
}
