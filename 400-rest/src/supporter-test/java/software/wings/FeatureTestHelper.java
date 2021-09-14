/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.persistence.HQuery.excludeAuthority;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.FeatureFlag;
import io.harness.beans.FeatureFlag.FeatureFlagKeys;
import io.harness.beans.FeatureName;
import io.harness.ff.FeatureFlagService;
import io.harness.persistence.HPersistence;

import software.wings.app.MainConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;

@Singleton
@OwnedBy(HarnessTeam.PL)
@TargetModule(HarnessModule._950_FEATURE_FLAG)
public class FeatureTestHelper {
  @Inject protected HPersistence persistence;
  @Inject protected FeatureFlagService featureFlagService;
  @Inject protected MainConfiguration mainConfiguration;

  public void enableFeatureFlag(FeatureName featureName) {
    featureFlagService.initializeFeatureFlags(mainConfiguration.getDeployMode(), mainConfiguration.getFeatureNames());
    persistence.update(
        persistence.createQuery(FeatureFlag.class, excludeAuthority).filter(FeatureFlagKeys.name, featureName),
        persistence.createUpdateOperations(FeatureFlag.class).set(FeatureFlagKeys.enabled, true));
    assertThat(featureFlagService.isEnabledReloadCache(featureName, generateUuid())).isTrue();
  }

  public void disableFeatureFlag(FeatureName featureName) {
    featureFlagService.initializeFeatureFlags(mainConfiguration.getDeployMode(), mainConfiguration.getFeatureNames());
    persistence.update(
        persistence.createQuery(FeatureFlag.class, excludeAuthority).filter(FeatureFlagKeys.name, featureName),
        persistence.createUpdateOperations(FeatureFlag.class)
            .set(FeatureFlagKeys.enabled, false)
            .set(FeatureFlagKeys.accountIds, Collections.emptyList()));
    assertThat(featureFlagService.isEnabledReloadCache(featureName, generateUuid()));
  }
}
