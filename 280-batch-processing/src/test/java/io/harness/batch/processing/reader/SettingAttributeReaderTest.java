/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.batch.processing.reader;

import static io.harness.rule.OwnerRule.ROHIT;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import software.wings.beans.SettingAttribute;

import com.google.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SettingAttributeReaderTest extends CategoryTest {
  @Inject @InjectMocks private SettingAttributeReader settingAttributeReader;
  @Mock AtomicBoolean runOnlyOnce;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    runOnlyOnce = new AtomicBoolean(false);
  }

  @Test
  @Owner(developers = ROHIT)
  @Category(UnitTests.class)
  public void testS3SyncRead() {
    SettingAttribute settingAttribute = settingAttributeReader.read();
    assertThat(settingAttribute).isNotNull();
    SettingAttribute secondReadOutput = settingAttributeReader.read();
    assertThat(secondReadOutput).isNull();
  }
}
