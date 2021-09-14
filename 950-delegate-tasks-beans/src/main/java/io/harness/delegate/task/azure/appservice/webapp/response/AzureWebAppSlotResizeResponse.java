/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.azure.appservice.webapp.response;

import io.harness.delegate.task.azure.appservice.AzureAppServicePreDeploymentData;
import io.harness.delegate.task.azure.appservice.AzureAppServiceTaskResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AzureWebAppSlotResizeResponse implements AzureAppServiceTaskResponse {
  private AzureAppServicePreDeploymentData preDeploymentData;
}
