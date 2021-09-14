/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.execution.export.request;

import static io.harness.rule.OwnerRule.GARVIT;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.exception.InvalidRequestException;
import io.harness.execution.export.request.ExportExecutionsRequestLimitChecks.LimitCheck;
import io.harness.rule.Owner;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ExportExecutionsRequestLimitChecksTest extends CategoryTest {
  @Test
  @Owner(developers = GARVIT)
  @Category(UnitTests.class)
  public void testValidate() {
    ExportExecutionsRequestLimitChecks limitChecks =
        ExportExecutionsRequestLimitChecks.builder()
            .queuedRequests(LimitCheck.builder().limit(10).value(5).build())
            .executionCount(LimitCheck.builder().limit(10).value(5).build())
            .build();
    assertThatCode(limitChecks::validate).doesNotThrowAnyException();

    limitChecks = ExportExecutionsRequestLimitChecks.builder()
                      .queuedRequests(LimitCheck.builder().limit(10).value(11).build())
                      .executionCount(LimitCheck.builder().limit(10).value(5).build())
                      .build();
    assertThatThrownBy(limitChecks::validate).isInstanceOf(InvalidRequestException.class);

    limitChecks = ExportExecutionsRequestLimitChecks.builder()
                      .queuedRequests(LimitCheck.builder().limit(10).value(5).build())
                      .executionCount(LimitCheck.builder().limit(10).value(11).build())
                      .build();
    assertThatThrownBy(limitChecks::validate).isInstanceOf(InvalidRequestException.class);

    limitChecks = ExportExecutionsRequestLimitChecks.builder()
                      .queuedRequests(LimitCheck.builder().limit(10).value(11).build())
                      .executionCount(LimitCheck.builder().limit(10).value(11).build())
                      .build();
    assertThatThrownBy(limitChecks::validate).isInstanceOf(InvalidRequestException.class);

    limitChecks = ExportExecutionsRequestLimitChecks.builder()
                      .queuedRequests(LimitCheck.builder().limit(10).value(5).build())
                      .executionCount(LimitCheck.builder().limit(10).value(0).build())
                      .build();
    assertThatThrownBy(limitChecks::validate).isInstanceOf(InvalidRequestException.class);

    limitChecks = ExportExecutionsRequestLimitChecks.builder()
                      .queuedRequests(LimitCheck.builder().limit(10).value(0).build())
                      .executionCount(LimitCheck.builder().limit(10).value(5).build())
                      .build();
    assertThatCode(limitChecks::validate).doesNotThrowAnyException();
  }
}
