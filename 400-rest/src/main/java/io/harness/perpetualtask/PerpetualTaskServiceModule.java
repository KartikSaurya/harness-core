/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.perpetualtask;

import io.harness.perpetualtask.example.SamplePTaskService;
import io.harness.perpetualtask.example.SamplePTaskServiceImpl;

import com.google.inject.AbstractModule;

public class PerpetualTaskServiceModule extends AbstractModule {
  private static volatile PerpetualTaskServiceModule instance;

  public static PerpetualTaskServiceModule getInstance() {
    if (instance == null) {
      instance = new PerpetualTaskServiceModule();
    }
    return instance;
  }

  @Override
  protected void configure() {
    bind(PerpetualTaskService.class).to(PerpetualTaskServiceImpl.class);
    bind(SamplePTaskService.class).to(SamplePTaskServiceImpl.class);
  }
}
