/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.exception;

import static io.harness.eraro.ErrorCode.DEPLOYMENT_MIGRATION_ERROR;

import io.harness.eraro.Level;

public class DeploymentMigrationException extends WingsException {
  private static final String MESSAGE_ARG = "message";

  public DeploymentMigrationException(String message, Throwable cause) {
    super(message, cause, DEPLOYMENT_MIGRATION_ERROR, Level.ERROR, null, null);
    super.param(MESSAGE_ARG, message);
  }
}
