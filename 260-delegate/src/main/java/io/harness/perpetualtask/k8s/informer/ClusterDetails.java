/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.perpetualtask.k8s.informer;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@OwnedBy(HarnessTeam.CE)
@Value
@Builder
@TargetModule(HarnessModule._420_DELEGATE_AGENT)
public class ClusterDetails {
  @NonNull String clusterId;
  @NonNull String cloudProviderId;
  @NonNull String clusterName;
  @Nonnull String kubeSystemUid;
  boolean isSeen;
}
