/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.k8s;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;

@OwnedBy(CDP)
public enum K8sTaskType {
  DEPLOYMENT_ROLLING,
  DEPLOYMENT_ROLLING_ROLLBACK,
  SCALE,
  CANARY_DEPLOY,
  BLUE_GREEN_DEPLOY,
  INSTANCE_SYNC,
  DELETE,
  TRAFFIC_SPLIT,
  APPLY,
  VERSION,
  SWAP_SERVICE_SELECTORS,
  CANARY_DELETE,
}
