/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.exception;

import static io.harness.eraro.ErrorCode.INVALID_CREDENTIAL;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.eraro.Level;

import java.util.EnumSet;

@OwnedBy(HarnessTeam.PIPELINE)
public class InvalidCredentialsException extends WingsException {
  private static final String MESSAGE_KEY = "message";

  public InvalidCredentialsException(String message, EnumSet<ReportTarget> reportTargets) {
    super(message, null, INVALID_CREDENTIAL, Level.ERROR, reportTargets, EnumSet.of(FailureType.AUTHENTICATION));
    super.param(MESSAGE_KEY, message);
  }

  public InvalidCredentialsException(String message, EnumSet<ReportTarget> reportTargets, Throwable cause) {
    super(message, cause, INVALID_CREDENTIAL, Level.ERROR, reportTargets, EnumSet.of(FailureType.AUTHENTICATION));
    super.param(MESSAGE_KEY, message);
  }
}
