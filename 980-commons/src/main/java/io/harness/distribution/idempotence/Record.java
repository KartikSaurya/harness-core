/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.distribution.idempotence;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(HarnessTeam.CDC)
class Record<T extends IdempotentResult> {
  enum InternalState {
    /*
     * Tentative currently running.
     */
    TENTATIVE,
    /*
     * Tentative currently running from another thread.
     */
    TENTATIVE_ALREADY,
    /*
     * Finished indicates it is already done.
     */
    FINISHED,
  }

  private InternalState state;
  private T result;
  private long validUntil;
}
