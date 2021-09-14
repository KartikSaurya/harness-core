/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.data.structure.UUIDGenerator.generateUuid;

import io.harness.CategoryTest;
import io.harness.MockableTestMixin;
import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import software.wings.beans.Account;
import software.wings.beans.Account.Builder;
import software.wings.beans.AccountStatus;
import software.wings.beans.AccountType;
import software.wings.beans.LicenseInfo;
import software.wings.rules.WingsRule;

import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@OwnedBy(PL)
@TargetModule(HarnessModule._980_COMMONS)
public abstract class WingsBaseTest extends CategoryTest implements MockableTestMixin {
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  // I am not absolutely sure why, but there is dependency between wings io.harness.rule and
  // MockitoJUnit io.harness.rule and they have to be listed in these order
  @Rule public WingsRule wingsRule = new WingsRule();

  protected static Account getAccount(String accountType) {
    Builder accountBuilder = Builder.anAccount().withUuid(generateUuid());
    LicenseInfo license = getLicenseInfo();
    license.setAccountType(accountType);
    accountBuilder.withLicenseInfo(license);

    return accountBuilder.build();
  }

  protected static LicenseInfo getLicenseInfo() {
    LicenseInfo licenseInfo = new LicenseInfo();
    licenseInfo.setAccountStatus(AccountStatus.ACTIVE);
    licenseInfo.setAccountType(AccountType.PAID);
    licenseInfo.setLicenseUnits(100);
    licenseInfo.setExpireAfterDays(15);
    return licenseInfo;
  }
}
