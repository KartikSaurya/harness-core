/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SampleAppEntityStatus {
  private String entityName;
  private String entityType;
  private Health health;
  public enum Health { GOOD, BAD }
  ;
}
