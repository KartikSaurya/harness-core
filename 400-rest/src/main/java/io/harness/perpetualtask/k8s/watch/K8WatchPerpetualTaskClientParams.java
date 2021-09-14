/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.perpetualtask.k8s.watch;

import io.harness.perpetualtask.PerpetualTaskClientParams;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class K8WatchPerpetualTaskClientParams implements PerpetualTaskClientParams {
  private String cloudProviderId;
  private String clusterId;
  private String clusterName;
}
