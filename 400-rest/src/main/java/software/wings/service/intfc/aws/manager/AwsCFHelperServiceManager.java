/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service.intfc.aws.manager;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;

import software.wings.service.impl.aws.model.AwsCFTemplateParamsData;

import java.util.List;

@OwnedBy(CDP)
public interface AwsCFHelperServiceManager {
  List<AwsCFTemplateParamsData> getParamsData(String type, String data, String awsConfigId, String region, String appId,
      String scmSettingId, String sourceRepoBranch, String templatePath, String commitId, Boolean useBranch,
      String repoName);
}
