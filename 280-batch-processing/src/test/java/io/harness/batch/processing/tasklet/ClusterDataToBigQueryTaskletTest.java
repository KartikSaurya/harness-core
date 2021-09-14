/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.batch.processing.tasklet;

import static io.harness.rule.OwnerRule.ROHIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.harness.batch.processing.billing.timeseries.data.InstanceBillingData;
import io.harness.batch.processing.billing.timeseries.service.impl.BillingDataServiceImpl;
import io.harness.batch.processing.ccm.BatchJobType;
import io.harness.batch.processing.ccm.CCMJobConstants;
import io.harness.batch.processing.config.BatchMainConfig;
import io.harness.batch.processing.service.impl.GoogleCloudStorageServiceImpl;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import io.harness.testsupport.BaseTaskletTest;

import software.wings.security.authentication.BatchQueryConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

@RunWith(MockitoJUnitRunner.class)
public class ClusterDataToBigQueryTaskletTest extends BaseTaskletTest {
  public static final String BILLING_DATA = "billing_data";
  public static final int BATCH_SIZE = 500;
  @Mock BillingDataServiceImpl billingDataService;
  @Mock private BatchMainConfig config;
  @Mock GoogleCloudStorageServiceImpl googleCloudStorageService;
  @InjectMocks ClusterDataToBigQueryTasklet clusterDataToBigQueryTasklet;
  @Mock private ChunkContext chunkContext;
  @Mock private StepContext stepContext;
  @Mock private StepExecution stepExecution;
  @Mock private JobParameters parameters;

  private final Instant END_INSTANT = Instant.now();
  private final Instant START_INSTANT = END_INSTANT.minus(1, ChronoUnit.HOURS);

  @Before
  public void setup() {
    InstanceBillingData instanceBillingData = InstanceBillingData.builder()
                                                  .startTimestamp(START_TIME_MILLIS)
                                                  .endTimestamp(END_TIME_MILLIS)
                                                  .accountId(ACCOUNT_ID)
                                                  .instanceId("instanceId")
                                                  .instanceType("instanceType")
                                                  .billingAmount(BigDecimal.ZERO)
                                                  .cpuBillingAmount(BigDecimal.ZERO)
                                                  .memoryBillingAmount(BigDecimal.ZERO)
                                                  .idleCost(BigDecimal.ZERO)
                                                  .cpuIdleCost(BigDecimal.ZERO)
                                                  .memoryIdleCost(BigDecimal.ZERO)
                                                  .systemCost(BigDecimal.ZERO)
                                                  .cpuSystemCost(BigDecimal.ZERO)
                                                  .memorySystemCost(BigDecimal.ZERO)
                                                  .actualIdleCost(BigDecimal.ZERO)
                                                  .cpuActualIdleCost(BigDecimal.ZERO)
                                                  .memoryActualIdleCost(BigDecimal.ZERO)
                                                  .unallocatedCost(BigDecimal.ZERO)
                                                  .cpuUnallocatedCost(BigDecimal.ZERO)
                                                  .memoryUnallocatedCost(BigDecimal.ZERO)
                                                  .storageBillingAmount(BigDecimal.ZERO)
                                                  .storageActualIdleCost(BigDecimal.ZERO)
                                                  .storageUnallocatedCost(BigDecimal.ZERO)
                                                  .storageUtilizationValue(0D)
                                                  .storageRequest(0D)
                                                  .maxStorageUtilizationValue(0D)
                                                  .maxStorageRequest(0D)
                                                  .build();

    when(config.getBatchQueryConfig()).thenReturn(BatchQueryConfig.builder().queryBatchSize(BATCH_SIZE).build());
    when(billingDataService.read(ACCOUNT_ID, Instant.ofEpochMilli(START_TIME_MILLIS),
             Instant.ofEpochMilli(END_TIME_MILLIS), BATCH_SIZE, 0, BatchJobType.CLUSTER_DATA_TO_BIG_QUERY))
        .thenReturn(Collections.singletonList(instanceBillingData));
  }

  @Test
  @Owner(developers = ROHIT)
  @Category(UnitTests.class)
  public void shouldExecute() throws Exception {
    when(chunkContext.getStepContext()).thenReturn(stepContext);
    when(stepContext.getStepExecution()).thenReturn(stepExecution);
    when(stepExecution.getJobParameters()).thenReturn(parameters);
    when(parameters.getString(CCMJobConstants.BATCH_JOB_TYPE))
        .thenReturn(BatchJobType.CLUSTER_DATA_TO_BIG_QUERY.name());
    when(parameters.getString(CCMJobConstants.JOB_START_DATE)).thenReturn(String.valueOf(START_INSTANT.toEpochMilli()));
    when(parameters.getString(CCMJobConstants.JOB_END_DATE)).thenReturn(String.valueOf(END_INSTANT.toEpochMilli()));
    RepeatStatus execute = clusterDataToBigQueryTasklet.execute(null, chunkContext);
    assertThat(execute).isNull();
  }
}
