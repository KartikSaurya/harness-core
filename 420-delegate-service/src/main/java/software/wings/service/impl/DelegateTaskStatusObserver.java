/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service.impl;

import static io.harness.annotations.dev.HarnessTeam.DEL;

import io.harness.annotations.dev.OwnedBy;

import software.wings.beans.DelegateTaskUsageInsightsEventType;

@OwnedBy(DEL)
public interface DelegateTaskStatusObserver {
  void onTaskAssigned(String accountId, String taskId, String delegateId, long taskTimeout);
  void onTaskCompleted(
      String accountId, String taskId, String delegateId, DelegateTaskUsageInsightsEventType eventType);
}
