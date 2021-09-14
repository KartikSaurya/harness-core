/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.perpetualtask.PerpetualTaskService;

import software.wings.beans.InfrastructureMapping;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.instance.InstanceService;

import com.google.inject.Inject;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
@OwnedBy(CDP)
public abstract class AbstractInstanceSyncPerpetualTaskCreator implements InstanceSyncPerpetualTaskCreator {
  @Inject InstanceService instanceService;
  @Inject PerpetualTaskService perpetualTaskService;
  @Inject AppService appService;
  @Inject ServiceResourceService serviceResourceService;
  @Inject EnvironmentService environmentService;

  @Nullable
  protected String getTaskDescription(InfrastructureMapping infraMapping) {
    try {
      return getTaskDescription(appService.get(infraMapping.getAppId()).getName(),
          serviceResourceService.get(infraMapping.getAppId(), infraMapping.getServiceId()).getName(),
          environmentService.get(infraMapping.getAppId(), infraMapping.getEnvId()).getName(),
          infraMapping.getDisplayName());
    } catch (Exception ex) {
      return null;
    }
  }

  private String getTaskDescription(String appName, String serviceName, String envName, String infraName) {
    return String.format("Application: [%s], Service: [%s], Environment: [%s], Infrastructure: [%s]", appName,
        serviceName, envName, infraName);
  }
}
