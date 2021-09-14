/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.delegatetasks.aws.ecs.ecstaskhandler.deploy;

import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.AwsConfig;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.SettingCategory;
import software.wings.beans.command.EcsResizeParams;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
class ContextData {
  private final AwsConfig awsConfig;
  private final List<EncryptedDataDetail> encryptedDataDetails;
  private final EcsResizeParams resizeParams;
  private final boolean deployingToHundredPercent;

  public SettingAttribute getSettingAttribute() {
    return SettingAttribute.Builder.aSettingAttribute()
        .withValue(awsConfig)
        .withCategory(SettingCategory.CLOUD_PROVIDER)
        .build();
  }
}
