/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ngtriggers.beans.entity.metadata;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OwnedBy(PIPELINE)
public class WebhookMetadata {
  String type;
  GitMetadata git;
  CustomMetadata custom;
  @Builder.Default WebhookRegistrationStatus registrationStatus = WebhookRegistrationStatus.UNAVAILABLE;
}
