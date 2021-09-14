/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.graphql.schema.type.aggregation;

import static io.harness.annotations.dev.HarnessTeam.CV;

import io.harness.annotations.dev.OwnedBy;

import software.wings.graphql.schema.type.QLEnum;

@OwnedBy(CV)
public enum QLExecutionStatusType implements QLEnum {
  ABORTED,
  ERROR,
  FAILED,
  RUNNING,
  SUCCESS,
  SKIPPED,
  EXPIRED;

  @Override
  public String getStringValue() {
    return this.name();
  }
}
