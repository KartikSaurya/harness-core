/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.setup.graphql;

import static io.harness.annotations.dev.HarnessTeam.CE;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import software.wings.security.PermissionAttribute;
import software.wings.security.annotations.Scope;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@Scope(PermissionAttribute.ResourceType.USER)
@FieldDefaults(level = AccessLevel.PRIVATE)
@OwnedBy(CE)
@TargetModule(HarnessModule._375_CE_GRAPHQL)
public class QLCEOverviewStatsData {
  Boolean ceEnabledClusterPresent;
  Boolean cloudConnectorsPresent;
  Boolean awsConnectorsPresent;
  Boolean gcpConnectorsPresent;
  Boolean azureConnectorsPresent;
  Boolean applicationDataPresent;
  Boolean inventoryDataPresent;
  Boolean clusterDataPresent;
  String defaultAzurePerspectiveId;
  String defaultAwsPerspectiveId;
  String defaultGcpPerspectiveId;
  String defaultClusterPerspectiveId;
  @Builder.Default Boolean isSampleClusterPresent = false;
}
