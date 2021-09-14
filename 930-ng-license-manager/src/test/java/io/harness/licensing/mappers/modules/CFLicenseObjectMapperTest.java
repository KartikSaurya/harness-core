/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.licensing.mappers.modules;

import static io.harness.rule.OwnerRule.ZHUO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.licensing.beans.modules.CFModuleLicenseDTO;
import io.harness.licensing.beans.modules.ModuleLicenseDTO;
import io.harness.licensing.entities.modules.CFModuleLicense;
import io.harness.licensing.entities.modules.ModuleLicense;
import io.harness.rule.Owner;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;

public class CFLicenseObjectMapperTest extends CategoryTest {
  @InjectMocks CFLicenseObjectMapper objectMapper;
  private CFModuleLicense moduleLicense;
  private CFModuleLicenseDTO moduleLicenseDTO;
  private static final int DEFAULT_USER_NUMBER = 2;
  private static final long DEFAULT_CLIENT_MAU = 5000;

  @Before
  public void setUp() {
    initMocks(this);
    moduleLicense =
        CFModuleLicense.builder().numberOfUsers(DEFAULT_USER_NUMBER).numberOfClientMAUs(DEFAULT_CLIENT_MAU).build();
    moduleLicenseDTO =
        CFModuleLicenseDTO.builder().numberOfUsers(DEFAULT_USER_NUMBER).numberOfClientMAUs(DEFAULT_CLIENT_MAU).build();
  }

  @Test
  @Owner(developers = ZHUO)
  @Category(UnitTests.class)
  public void testToDTO() {
    ModuleLicenseDTO result = objectMapper.toDTO(moduleLicense);
    assertThat(result).isEqualTo(moduleLicenseDTO);
  }

  @Test
  @Owner(developers = ZHUO)
  @Category(UnitTests.class)
  public void testToEntity() {
    ModuleLicense result = objectMapper.toEntity(moduleLicenseDTO);
    assertThat(result).isEqualTo(moduleLicense);
  }
}
