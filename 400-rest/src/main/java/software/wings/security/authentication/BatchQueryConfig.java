/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.security.authentication;

import com.google.inject.Singleton;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Singleton
public class BatchQueryConfig {
  private int queryBatchSize;
  private int instanceDataBatchSize;
  private boolean syncJobDisabled;
}
