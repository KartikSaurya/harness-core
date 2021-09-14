/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.integration.dl;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class DummyItem {
  int i;
  String s;
}
