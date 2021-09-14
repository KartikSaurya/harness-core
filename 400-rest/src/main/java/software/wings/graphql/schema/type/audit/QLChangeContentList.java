/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.graphql.schema.type.audit;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import software.wings.security.PermissionAttribute.ResourceType;
import software.wings.security.annotations.Scope;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Scope(ResourceType.APPLICATION)
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public class QLChangeContentList {
  private List<QLChangeContent> data;
}
