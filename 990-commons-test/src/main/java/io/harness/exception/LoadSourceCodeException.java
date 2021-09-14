/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.exception;

public class LoadSourceCodeException extends RuntimeException {
  public LoadSourceCodeException(Exception e) {
    super(e);
  }
}
