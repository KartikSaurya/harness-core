/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.batch.processing.tasklet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HarnessTags {
  private String key;
  private String value;
}
