/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.api;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.rule.OwnerRule.ANSHUL;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import java.util.Map;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(CDP)
public class AwsAmiSetupExecutionDataTest extends CategoryTest {
  private AwsAmiSetupExecutionData awsAmiSetupExecutionData =
      AwsAmiSetupExecutionData.builder().maxInstances(2).desiredInstances(3).build();

  @Test
  @Owner(developers = ANSHUL)
  @Category(UnitTests.class)
  public void testGetExecutionSummary() {
    Map<String, ExecutionDataValue> executionDetails = awsAmiSetupExecutionData.getExecutionDetails();
    assertThat(executionDetails).isNotEmpty();
    assertThat(executionDetails).containsKey("desiredInstances");
    assertThat(executionDetails.get("desiredInstances").getDisplayName()).isEqualTo("Desired Instances");
    assertThat(executionDetails.get("desiredInstances").getValue()).isEqualTo(3);

    assertThat(executionDetails).containsKey("maxInstances");
    assertThat(executionDetails.get("maxInstances").getDisplayName()).isEqualTo("Max Instances");
    assertThat(executionDetails.get("maxInstances").getValue()).isEqualTo(2);
  }
}
