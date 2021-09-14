/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ng.core;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NextGenConfig {
  String managerServiceSecret;
  String userVerificationSecret;
  String ngManagerServiceSecret;
  String pipelineServiceSecret;
  String jwtAuthSecret;
  String jwtIdentityServiceSecret;
}
