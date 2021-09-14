/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.service.intf;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

@OwnedBy(HarnessTeam.CE)
public interface AWSBucketPolicyHelperService {
  boolean updateBucketPolicy(String crossAccountRoleArn, String awsS3Bucket, String awsAccessKey, String awsSecretKey);
}
