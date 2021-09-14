/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.batch.processing.k8s.rcd;

import static io.harness.rule.OwnerRule.AVMOHAN;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import com.google.common.collect.ImmutableMap;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.util.Yaml;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PodRcdCalculatorTest extends CategoryTest {
  @Test
  @Owner(developers = AVMOHAN)
  @Category(UnitTests.class)
  public void shouldHandleAdd() throws Exception {
    assertThat(new PodRcdCalculator().computeResourceClaimDiff("", podYaml("100m", "1200Mi")).getDiff())
        .isEqualTo(ResourceClaim.builder().cpuNano(100000000L).memBytes(1258291200L).build());
  }

  @Test
  @Owner(developers = AVMOHAN)
  @Category(UnitTests.class)
  public void shouldHandleDelete() throws Exception {
    assertThat(new PodRcdCalculator().computeResourceClaimDiff(podYaml("750m", "1300Mi"), "").getDiff())
        .isEqualTo(ResourceClaim.builder().cpuNano(-750000000L).memBytes(-1363148800L).build());
  }

  @Test
  @Owner(developers = AVMOHAN)
  @Category(UnitTests.class)
  public void shouldHandleUpdate() throws Exception {
    assertThat(
        new PodRcdCalculator().computeResourceClaimDiff(podYaml("0.12", "1300Mi"), podYaml("100m", "1200Mi")).getDiff())
        .isEqualTo(ResourceClaim.builder().cpuNano(-20000000L).memBytes(-104857600L).build());
  }
  private String podYaml(String cpu, String memory) {
    return Yaml.dump(
        new V1PodBuilder()
            .withNewSpec()
            .addNewContainer()
            .withNewResources()
            .withRequests(ImmutableMap.of("cpu", Quantity.fromString(cpu), "memory", Quantity.fromString(memory)))
            .endResources()
            .endContainer()
            .endSpec()
            .build());
  }
}
