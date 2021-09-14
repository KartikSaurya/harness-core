/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.persistence;

import io.harness.beans.EmbeddedUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(innerTypeName = "TestEntityCreatedAwareKeys")
class TestEntityCreatedAware implements PersistentEntity, UuidAccess, CreatedAtAware, CreatedByAware {
  @Id private String uuid;
  private long createdAt;
  private EmbeddedUser createdBy;

  private String test;
}
