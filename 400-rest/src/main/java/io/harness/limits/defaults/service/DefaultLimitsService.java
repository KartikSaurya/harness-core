/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.limits.defaults.service;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.limits.ActionType;
import io.harness.limits.lib.Limit;

@OwnedBy(PL)
public interface DefaultLimitsService {
  // GraphQL external/internal API call's default rate limit constants.
  int GRAPHQL_EXTERNAL_RATE_LIMIT_ACCOUNT_DEFAULT = 30;
  int GRAPHQL_EXTERNAL_RATE_LIMIT_COMMUNITY_ACCOUNT_DEFAULT = 5;
  int GRAPHQL_INTERNAL_RATE_LIMIT_ACCOUNT_DEFAULT = 60;
  int GRAPHQL_INTERNAL_RATE_LIMIT_COMMUNITY_ACCOUNT_DEFAULT = 5;

  int GRAPHQL_RATE_LIMIT_DURATION_IN_MINUTE = 1;

  int EXPORT_EXECUTIONS_LIMIT_PER_DAY = 25;

  Limit get(ActionType actionType, String accountType);
}
