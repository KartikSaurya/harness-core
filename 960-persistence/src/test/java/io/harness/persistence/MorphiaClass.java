/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.persistence;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MorphiaClass implements MorphiaInterface {
  private String test;
}
