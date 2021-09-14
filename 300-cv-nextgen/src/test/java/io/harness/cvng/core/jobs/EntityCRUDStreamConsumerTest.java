/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cvng.core.jobs;

import static io.harness.rule.OwnerRule.ABHIJITH;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import io.harness.CvNextGenTestBase;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.eventsframework.api.Consumer;
import io.harness.queue.QueueController;
import io.harness.rule.Owner;

import com.google.inject.Inject;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.CV)
public class EntityCRUDStreamConsumerTest extends CvNextGenTestBase {
  @Inject private EntityCRUDStreamConsumer entityCRUDStreamConsumer;

  @Mock private Consumer consumer;
  @Mock private QueueController queueController;

  @Before
  public void setup() throws IllegalAccessException {
    MockitoAnnotations.initMocks(this);
    when(queueController.isNotPrimary()).thenReturn(false);
    FieldUtils.writeField(entityCRUDStreamConsumer, "queueController", queueController, true);
    FieldUtils.writeField(entityCRUDStreamConsumer, "consumer", consumer, true);
  }

  @Test
  @Owner(developers = ABHIJITH)
  @Category(UnitTests.class)
  public void testStreamConsumerOnPrimaryMachine() throws InterruptedException {
    runConsumerFor10ms();
    verify(consumer, atLeast(1)).read(Mockito.any());
  }

  @Test
  @Owner(developers = ABHIJITH)
  @Category(UnitTests.class)
  public void testStreamConsumerOnNonPrimaryMachine() throws InterruptedException {
    when(queueController.isNotPrimary()).thenReturn(true);
    runConsumerFor10ms();
    verifyZeroInteractions(consumer);
  }

  private void runConsumerFor10ms() throws InterruptedException {
    Thread t = new Thread(entityCRUDStreamConsumer);
    t.start();
    TimeUnit.MILLISECONDS.sleep(10);
    t.interrupt();
  }
}
