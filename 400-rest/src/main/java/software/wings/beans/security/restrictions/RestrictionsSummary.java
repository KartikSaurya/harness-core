/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans.security.restrictions;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * @author rktummala on 06/24/18
 */
@OwnedBy(PL)
@Data
@Builder
public class RestrictionsSummary {
  private boolean hasAllAppAccess;
  private boolean hasAllProdEnvAccess;
  private boolean hasAllNonProdEnvAccess;
  private Set<AppRestrictionsSummary> applications;
}
