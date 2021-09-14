/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.execution.export.metadata;

import static io.harness.rule.OwnerRule.GARVIT;

import static software.wings.beans.ElementExecutionSummary.ElementExecutionSummaryBuilder.anElementExecutionSummary;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import software.wings.api.CloudProviderType;
import software.wings.api.DeploymentType;
import software.wings.sm.InfraDefinitionSummary;

import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class InfraMetadataTest extends CategoryTest {
  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void testFromElementExecutionSummary() {
    assertThat(InfraMetadata.fromElementExecutionSummary(null)).isNull();

    List<InfraMetadata> infraMetadataList =
        InfraMetadata.fromElementExecutionSummary(anElementExecutionSummary()
                                                      .withInfraDefinitionSummaries(asList(null,
                                                          InfraDefinitionSummary.builder()
                                                              .displayName("n")
                                                              .cloudProviderName("cpn")
                                                              .cloudProviderType(CloudProviderType.AWS)
                                                              .deploymentType(DeploymentType.SSH)
                                                              .build()))
                                                      .build());
    assertThat(infraMetadataList).isNotNull();
    assertThat(infraMetadataList.size()).isEqualTo(1);
    validateInfraMetadata(infraMetadataList.get(0));
  }

  private void validateInfraMetadata(InfraMetadata infraMetadata) {
    assertThat(infraMetadata).isNotNull();
    assertThat(infraMetadata.getName()).isEqualTo("n");
    assertThat(infraMetadata.getCloudProviderName()).isEqualTo("cpn");
    assertThat(infraMetadata.getCloudProviderType()).isEqualTo(CloudProviderType.AWS.name());
    assertThat(infraMetadata.getDeploymentType()).isEqualTo(DeploymentType.SSH.getDisplayName());
  }
}
