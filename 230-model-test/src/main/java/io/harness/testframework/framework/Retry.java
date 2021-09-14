/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.testframework.framework;

import io.harness.testframework.framework.matchers.Matcher;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Retry<T> {
  private int retryCounter;
  private int maxRetries;
  private int introduceDelayInMS;

  public Retry(int maxRetries, int introduceDelayInMS) {
    this.maxRetries = maxRetries;
    this.introduceDelayInMS = introduceDelayInMS;
  }

  public Retry(int maxRetries) {
    this.maxRetries = maxRetries;
    this.introduceDelayInMS = 0;
  }

  // Takes a function and executes it, if fails, passes the function to the retry command
  public T executeWithRetry(Supplier<T> function, Matcher<T> matcher, T expected) {
    return retry(function, matcher, expected);
  }

  public int getRetryCounter() {
    return retryCounter;
  }

  private T retry(Supplier<T> function, Matcher<T> matcher, T expected) throws RuntimeException {
    log.info("Execution will be retried : " + maxRetries + " times.");
    retryCounter = 0;
    T actual;
    while (retryCounter < maxRetries) {
      log.info("Retry Attempt : " + retryCounter);
      try {
        TimeUnit.MILLISECONDS.sleep(this.introduceDelayInMS);
        actual = function.get();
        if (matcher.matches(expected, actual)) {
          return actual;
        }
      } catch (Exception ex) {
        log.info("Execution failed on retry " + retryCounter + " of " + maxRetries + " error: " + ex);
        if (retryCounter >= maxRetries) {
          log.warn("Max retries exceeded.");
          break;
        }
      }
      retryCounter++;
    }
    throw new RuntimeException("Command failed on all of " + maxRetries + " retries");
  }
}
