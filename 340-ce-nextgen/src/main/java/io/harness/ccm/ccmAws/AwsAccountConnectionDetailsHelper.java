/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.ccmAws;

import static io.harness.annotations.dev.HarnessTeam.CE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ccm.CENextGenConfiguration;
import io.harness.ccm.commons.entities.AwsAccountConnectionDetail;

import com.google.inject.Inject;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OwnedBy(CE)
public class AwsAccountConnectionDetailsHelper {
  @Inject CENextGenConfiguration configuration;

  private static final String EXTERNAL_ID_TEMPLATE = "harness:%s:%s";
  private static final String stackBaseTemplate =
      "https://console.aws.amazon.com/cloudformation/home?#/stacks/quickcreate?stackName=%s&templateURL=%s";
  private static final String stackNameValue = "harness-ce-iam-role-stack";
  public AwsAccountConnectionDetail getAwsAccountConnectorDetail(String accountId) {
    String harnessAccountId = configuration.getAwsConfig().getHarnessAwsAccountId();
    String externalId = String.format(EXTERNAL_ID_TEMPLATE, getProcessedAccountId(harnessAccountId), accountId);
    String stackLaunchTemplateLink =
        String.format(stackBaseTemplate, stackNameValue, configuration.getAwsConfig().getAwsConnectorTemplate());
    return AwsAccountConnectionDetail.builder()
        .externalId(externalId)
        .harnessAccountId(getProcessedAccountId(harnessAccountId))
        .cloudFormationTemplateLink(configuration.getAwsConfig().getAwsConnectorTemplate())
        .stackLaunchTemplateLink(stackLaunchTemplateLink)
        .build();
  }

  public static String getProcessedAccountId(String awsAccountId) {
    try {
      awsAccountId = String.valueOf(new BigDecimal(awsAccountId).longValue());
    } catch (Exception ex) {
      log.error("Exception while getting accountId {}", ex);
    }
    return awsAccountId;
  }
}
