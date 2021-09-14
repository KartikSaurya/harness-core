/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.task.spotinst.request;

import static io.harness.delegate.task.spotinst.request.SpotInstTaskParameters.SpotInstTaskType.SPOT_INST_SWAP_ROUTES;

import io.harness.delegate.task.aws.LoadBalancerDetailsForBGDeployment;
import io.harness.spotinst.model.ElastiGroup;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpotInstSwapRoutesTaskParameters extends SpotInstTaskParameters {
  private ElastiGroup newElastiGroup;
  private ElastiGroup oldElastiGroup;
  private String elastiGroupNamePrefix;
  private boolean downsizeOldElastiGroup;
  private boolean rollback;
  private int steadyStateTimeOut;
  private List<LoadBalancerDetailsForBGDeployment> lBdetailsForBGDeploymentList;

  @Builder
  public SpotInstSwapRoutesTaskParameters(String appId, String accountId, String activityId, String commandName,
      String workflowExecutionId, Integer timeoutIntervalInMin, String awsRegion, ElastiGroup newElastiGroup,
      ElastiGroup oldElastiGroup, boolean downsizeOldElastiGroup, boolean rollback, String elastiGroupNamePrefix,
      int steadyStateTimeOut, List<LoadBalancerDetailsForBGDeployment> lBdetailsForBGDeploymentList) {
    super(appId, accountId, activityId, commandName, workflowExecutionId, timeoutIntervalInMin, SPOT_INST_SWAP_ROUTES,
        awsRegion);
    this.newElastiGroup = newElastiGroup;
    this.oldElastiGroup = oldElastiGroup;
    this.downsizeOldElastiGroup = downsizeOldElastiGroup;
    this.rollback = rollback;
    this.elastiGroupNamePrefix = elastiGroupNamePrefix;
    this.steadyStateTimeOut = steadyStateTimeOut;
    this.lBdetailsForBGDeploymentList = lBdetailsForBGDeploymentList;
  }
}
