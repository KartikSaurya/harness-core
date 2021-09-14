/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cdng.provision.terraform;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.expressions.CDExpressionResolveFunctor;
import io.harness.cdng.provision.terraform.TerraformConfig.TerraformConfigKeys;
import io.harness.expression.ExpressionEvaluatorUtils;
import io.harness.persistence.HPersistence;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.expression.EngineExpressionSecretUtils;
import io.harness.pms.expression.EngineExpressionService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.query.Query;

@Slf4j
@Singleton
@OwnedBy(HarnessTeam.CDP)
public class TerraformConfigDAL {
  @Inject private HPersistence persistence;
  @Inject private EngineExpressionService engineExpressionService;

  void saveTerraformConfig(@Nonnull TerraformConfig terraformConfig) {
    // NG Secret Manager Functor is reversed to secret.getValue form so that token is not saved inside DB
    TerraformConfig secretsRevertedConfig =
        (TerraformConfig) EngineExpressionSecretUtils.revertSecrets(terraformConfig);
    persistence.save(secretsRevertedConfig);
  }

  TerraformConfig getTerraformConfig(@Nonnull Query<TerraformConfig> query, @Nonnull Ambiance ambiance) {
    TerraformConfig terraformConfig = query.get();

    if (terraformConfig == null) {
      return null;
    }
    ExpressionEvaluatorUtils.updateExpressions(
        terraformConfig, new CDExpressionResolveFunctor(engineExpressionService, ambiance));

    return terraformConfig;
  }

  public void clearTerraformConfig(@Nonnull Ambiance ambiance, @Nonnull String entityId) {
    persistence.delete(persistence.createQuery(TerraformConfig.class)
                           .filter(TerraformConfigKeys.accountId, AmbianceUtils.getAccountId(ambiance))
                           .filter(TerraformConfigKeys.orgId, AmbianceUtils.getOrgIdentifier(ambiance))
                           .filter(TerraformConfigKeys.projectId, AmbianceUtils.getProjectIdentifier(ambiance))
                           .filter(TerraformConfigKeys.entityId, entityId));
  }
}
