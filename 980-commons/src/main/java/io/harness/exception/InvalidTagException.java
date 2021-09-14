/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.exception;

import static io.harness.eraro.ErrorCode.IMAGE_TAG_NOT_FOUND;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.eraro.Level;

import java.util.EnumSet;

@OwnedBy(HarnessTeam.PIPELINE)
public class InvalidTagException extends WingsException {
  private static final String MESSAGE_KEY = "message";

  public InvalidTagException(String message, EnumSet<ReportTarget> reportTargets) {
    super(message, null, IMAGE_TAG_NOT_FOUND, Level.ERROR, reportTargets, EnumSet.of(FailureType.APPLICATION_ERROR));
    param(MESSAGE_KEY, message);
  }

  public InvalidTagException(String message, Level level, EnumSet<ReportTarget> reportTargets) {
    super(message, null, IMAGE_TAG_NOT_FOUND, level, reportTargets, EnumSet.of(FailureType.APPLICATION_ERROR));
    param(MESSAGE_KEY, message);
  }
}
