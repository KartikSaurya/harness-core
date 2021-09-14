/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.grpc.pingpong;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.service.DelegateAgentServiceImpl;
import io.harness.event.Ping;
import io.harness.event.PingPongServiceGrpc.PingPongServiceBlockingStub;
import io.harness.grpc.utils.HTimestamps;
import io.harness.logging.LoggingListener;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.ProcessIdUtil;

@Slf4j
@TargetModule(HarnessModule._420_DELEGATE_AGENT)
public class PingPongClient extends AbstractScheduledService {
  private static final String PROCESS_ID = ProcessIdUtil.getProcessId();
  private final PingPongServiceBlockingStub pingPongServiceBlockingStub;
  private final String version;

  PingPongClient(PingPongServiceBlockingStub pingPongServiceBlockingStub, String version) {
    this.pingPongServiceBlockingStub = pingPongServiceBlockingStub;
    this.version = version;
    addListener(new LoggingListener(this), MoreExecutors.directExecutor());
  }

  @Override
  protected void runOneIteration() {
    try {
      Instant timestamp = Instant.now();
      Ping ping = Ping.newBuilder()
                      .setDelegateId(DelegateAgentServiceImpl.getDelegateId().orElse("UNREGISTERED"))
                      .setPingTimestamp(HTimestamps.fromInstant(timestamp))
                      .setProcessId(PROCESS_ID)
                      .setVersion(version)
                      .build();
      pingPongServiceBlockingStub.withDeadlineAfter(5, TimeUnit.SECONDS).tryPing(ping);
      log.info("Ping at {} successful", timestamp);
    } catch (Exception e) {
      log.error("Ping failed", e);
    }
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedDelaySchedule(5, 5, TimeUnit.MINUTES);
  }
}
