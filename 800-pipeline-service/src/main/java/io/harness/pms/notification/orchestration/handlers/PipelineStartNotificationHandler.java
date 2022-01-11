/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.notification.orchestration.handlers;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.observers.OrchestrationStartObserver;
import io.harness.engine.observers.beans.OrchestrationStartInfo;
import io.harness.notification.PipelineEventType;
import io.harness.observer.AsyncInformObserver;
import io.harness.pms.notification.NotificationHelper;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.concurrent.ExecutorService;

@OwnedBy(HarnessTeam.PIPELINE)
public class PipelineStartNotificationHandler implements AsyncInformObserver, OrchestrationStartObserver {
  @Inject @Named("PipelineExecutorService") ExecutorService executorService;

  @Inject NotificationHelper notificationHelper;

  @Override
  public void onStart(OrchestrationStartInfo orchestrationStartInfo) {
    notificationHelper.sendNotification(
        orchestrationStartInfo.getAmbiance(), PipelineEventType.PIPELINE_START, null, null);
  }

  @Override
  public ExecutorService getInformExecutorService() {
    return executorService;
  }
}
