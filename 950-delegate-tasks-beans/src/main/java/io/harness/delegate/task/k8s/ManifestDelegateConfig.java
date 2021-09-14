/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.k8s;

import io.harness.delegate.beans.storeconfig.StoreDelegateConfig;

public interface ManifestDelegateConfig {
  ManifestType getManifestType();
  StoreDelegateConfig getStoreDelegateConfig();
}
