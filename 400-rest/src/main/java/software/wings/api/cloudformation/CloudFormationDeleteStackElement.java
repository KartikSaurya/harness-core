/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.api.cloudformation;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.context.ContextElementType;

import software.wings.sm.ContextElement;
import software.wings.sm.ExecutionContext;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(CDP)
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
public class CloudFormationDeleteStackElement implements CloudFormationElement {
  @Override
  public ContextElementType getElementType() {
    return ContextElementType.CLOUD_FORMATION_DEPROVISION;
  }

  @Override
  public String getUuid() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Map<String, Object> paramMap(ExecutionContext context) {
    return null;
  }

  @Override
  public ContextElement cloneMin() {
    return null;
  }
}
