/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.azure.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AzureVMInstanceData {
  private String instanceId;
  private String publicDnsName;
  private String privateDnsName;
  private String privateIpAddress;
}
