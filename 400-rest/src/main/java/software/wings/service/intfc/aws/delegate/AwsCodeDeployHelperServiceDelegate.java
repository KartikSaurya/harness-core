/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service.intfc.aws.delegate;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.AwsConfig;
import software.wings.service.impl.aws.model.AwsCodeDeployS3LocationData;

import com.amazonaws.services.ec2.model.Instance;
import java.util.List;

@TargetModule(HarnessModule._930_DELEGATE_TASKS)
@OwnedBy(CDP)
public interface AwsCodeDeployHelperServiceDelegate {
  List<String> listApplications(AwsConfig awsConfig, List<EncryptedDataDetail> encryptionDetails, String region);
  List<String> listDeploymentConfiguration(
      AwsConfig awsConfig, List<EncryptedDataDetail> encryptedDataDetails, String region);
  List<String> listDeploymentGroups(
      AwsConfig awsConfig, List<EncryptedDataDetail> encryptedDataDetails, String region, String appName);
  List<Instance> listDeploymentInstances(
      AwsConfig awsConfig, List<EncryptedDataDetail> encryptedDataDetails, String region, String deploymentId);
  AwsCodeDeployS3LocationData listAppRevision(AwsConfig awsConfig, List<EncryptedDataDetail> encryptedDataDetails,
      String region, String appName, String deploymentGroupName);
}
