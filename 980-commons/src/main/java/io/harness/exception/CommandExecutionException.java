/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.exception;

import static io.harness.eraro.ErrorCode.COMMAND_EXECUTION_ERROR;

import io.harness.eraro.Level;

public class CommandExecutionException extends WingsException {
  private static final String REASON_ARG = "reason";

  public CommandExecutionException(String reason) {
    this(reason, null);
  }

  public CommandExecutionException(String reason, Throwable cause) {
    super(null, cause, COMMAND_EXECUTION_ERROR, Level.ERROR, null, null);
    super.param(REASON_ARG, reason);
  }
}
