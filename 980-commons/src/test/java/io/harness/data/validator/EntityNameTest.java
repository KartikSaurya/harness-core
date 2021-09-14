/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.data.validator;

import static io.harness.data.validator.EntityNameValidator.ALLOWED_CHARS_SERVICE_VARIABLE_MESSAGE;
import static io.harness.data.validator.EntityNameValidator.ALLOWED_CHARS_SERVICE_VARIABLE_STRING;
import static io.harness.rule.OwnerRule.SATYAM;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import lombok.Builder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class EntityNameTest extends CategoryTest {
  @Builder
  static class EntityNameTestStructure {
    @EntityName String str;
  }

  @Builder
  static class EntityNameServicaVariableTestStructure {
    @EntityName(charSetString = ALLOWED_CHARS_SERVICE_VARIABLE_STRING, message = ALLOWED_CHARS_SERVICE_VARIABLE_MESSAGE)
    private String str;
  }

  @Test
  @Owner(developers = SATYAM)
  @Category(UnitTests.class)
  public void testAllowedCharSet() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    String nonAllowed = "!@#$%^&*()+=\\/[]{}|~";
    for (char ch : nonAllowed.toCharArray()) {
      assertThat(validator.validate(EntityNameTestStructure.builder().str(String.format("foo%c", ch)).build()))
          .isNotEmpty();
    }
    String allowed = "_- ";
    for (char ch : allowed.toCharArray()) {
      assertThat(validator.validate(EntityNameTestStructure.builder().str(String.format("foo%c", ch)).build()))
          .isEmpty();
    }
    assertThat(validator.validate(EntityNameServicaVariableTestStructure.builder().str(String.format("foo%c", '-'))))
        .isEmpty();
  }
}
