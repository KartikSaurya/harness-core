/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.notification.channelDetails;

import io.harness.Team;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.notification.channeldetails.NotificationChannel;
import io.harness.notification.channeldetails.PagerDutyChannel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@OwnedBy(HarnessTeam.PIPELINE)
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(NotificationChannelType.PAGERDUTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsPagerDutyChannel extends PmsNotificationChannel {
  List<String> userGroups;
  String integrationKey;
  @Override
  public NotificationChannel toNotificationChannel(String accountId, String orgIdentifier, String projectIdentifier,
      String templateId, Map<String, String> templateData) {
    return PagerDutyChannel.builder()
        .accountId(accountId)
        .userGroups(
            userGroups.stream()
                .map(e -> NotificationChannelUtils.getUserGroups(e, accountId, orgIdentifier, projectIdentifier))
                .collect(Collectors.toList()))
        .team(Team.PIPELINE)
        .templateId(templateId)
        .integrationKeys(Lists.newArrayList(integrationKey))
        .templateData(templateData)
        .build();
  }
}
