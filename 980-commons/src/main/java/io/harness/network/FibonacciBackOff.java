/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.network;

import static io.harness.annotations.dev.HarnessTeam.DEL;
import static io.harness.threading.Morpheus.sleep;

import io.harness.annotations.dev.OwnedBy;

import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(DEL)
@Slf4j
public final class FibonacciBackOff {
  private static final int[] FIBONACCI = new int[] {1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};

  private FibonacciBackOff() {}

  public static int getFibonacciElement(int index) {
    if (index < FIBONACCI.length) {
      return FIBONACCI[index];
    }
    throw new RuntimeException("Index out of bounds");
  }

  public static <T, E extends Exception> T execute(FibonacciBackOffFunction<T> fn) throws IOException {
    for (int attempt = 0; attempt < FIBONACCI.length; attempt++) {
      try {
        return fn.execute();
      } catch (IOException e) {
        handleFailure(attempt, e);
      }
    }
    throw new RuntimeException("Failed to communicate.");
  }

  public static <T> T executeForEver(FibonacciBackOffFunction<T> fn) throws IOException {
    for (int attempt = 0; attempt < FIBONACCI.length; attempt++) {
      try {
        return fn.execute();
      } catch (IOException e) {
        handleFailure(attempt, e);
      }
    }
    int attempt = FIBONACCI.length - 1;
    while (true) {
      try {
        return fn.execute();
      } catch (IOException e) {
        handleFailure(attempt, e);
      }
    }
  }

  private static void handleFailure(int attempt, IOException e) {
    log.error("error while executing", e);
    sleep(Duration.ofSeconds(FIBONACCI[attempt]));
  }

  public static IOException peel(IOException t) {
    while (t.getCause() != null && t.getCause() instanceof IOException) {
      t = (IOException) t.getCause();
    }
    return t;
  }
}
