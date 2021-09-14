/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.k8s.beans;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.sdk.core.data.ExecutionSweepingOutput;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.TypeAlias;

@Value
@Builder
@OwnedBy(HarnessTeam.CDP)
@TypeAlias("k8sCanaryExecutionOutput")
@JsonTypeName("k8sCanaryExecutionOutput")
@RecasterAlias("io.harness.cdng.k8s.beans.K8sCanaryExecutionOutput")
public class K8sCanaryExecutionOutput implements ExecutionSweepingOutput {
  public static final String OUTPUT_NAME = "k8sCanaryExecutionOutput";
}
