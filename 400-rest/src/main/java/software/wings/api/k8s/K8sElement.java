/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.api.k8s;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class K8sElement {
  String releaseName;
  Integer releaseNumber;
  Integer targetInstances;
  String primaryServiceName;
  String stageServiceName;
  String currentReleaseWorkload;
  String previousReleaseWorkload;
  String canaryWorkload;
  boolean isCanary;
}
