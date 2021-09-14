/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.batch.processing.config;

import io.harness.batch.processing.ccm.BatchJobType;
import io.harness.batch.processing.reader.SettingAttributeReader;
import io.harness.batch.processing.writer.S3SyncEventWriter;

import software.wings.beans.SettingAttribute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class S3SyncJobConfig {
  private static final int BATCH_SIZE = 10;

  @Autowired private JobBuilderFactory jobBuilderFactory;
  @Autowired private StepBuilderFactory stepBuilderFactory;

  @Bean
  @Qualifier(value = "s3SyncJob")
  public Job s3SyncJob(JobBuilderFactory jobBuilderFactory, Step s3SyncStep) {
    return jobBuilderFactory.get(BatchJobType.SYNC_BILLING_REPORT_S3.name())
        .incrementer(new RunIdIncrementer())
        .start(s3SyncStep)
        .build();
  }

  @Bean
  public Step s3SyncStep(StepBuilderFactory stepBuilderFactory, SettingAttributeReader settingAttributeReader) {
    return stepBuilderFactory.get("s3SyncStep")
        .<SettingAttribute, SettingAttribute>chunk(BATCH_SIZE)
        .reader(settingAttributeReader)
        .writer(s3SyncWriter())
        .build();
  }

  @Bean
  public ItemWriter<SettingAttribute> s3SyncWriter() {
    return new S3SyncEventWriter();
  }
}
