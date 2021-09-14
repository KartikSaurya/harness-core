/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.sdk;

import static io.harness.rule.OwnerRule.BRIJESH;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import io.harness.ModuleType;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.pms.contracts.plan.PlanCreationServiceGrpc;
import io.harness.rule.Owner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.PIPELINE)
public class PmsSdkHelperTest {
  @Mock private Map<ModuleType, PlanCreationServiceGrpc.PlanCreationServiceBlockingStub> planCreatorServices;
  @Mock private PmsSdkInstanceService pmsSdkInstanceService;
  @InjectMocks PmsSdkHelper pmsSdkHelper;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testGetServices() {
    Map<String, Map<String, Set<String>>> sdkInstances = new HashMap<>();
    sdkInstances.put("CD", new HashMap<>());
    sdkInstances.get("CD").put("key", Collections.singleton("value"));
    doReturn(sdkInstances).when(pmsSdkInstanceService).getInstanceNameToSupportedTypes();
    assertEquals(pmsSdkHelper.getServices().size(), 0);
    doReturn(true).when(planCreatorServices).containsKey(any());
    assertEquals(pmsSdkHelper.getServices().size(), 1);
  }
}
