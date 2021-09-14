/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.search.framework;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

@OwnedBy(PL)
class BoundedExecutorService {
  private final ExecutorService executorService;
  private final Semaphore semaphore;

  BoundedExecutorService(ExecutorService executorService, int bound) {
    this.executorService = executorService;
    this.semaphore = new Semaphore(bound);
  }

  <T> Future<T> submit(final Callable<T> task) throws InterruptedException {
    semaphore.acquire();
    try {
      return executorService.submit(() -> {
        try {
          return task.call();
        } finally {
          semaphore.release();
        }
      });
    } catch (RejectedExecutionException e) {
      semaphore.release();
      throw e;
    }
  }
}
