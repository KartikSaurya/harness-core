/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.graphql.core.recommendation;

import io.harness.ccm.commons.beans.recommendation.NodePoolId;
import io.harness.ccm.commons.beans.recommendation.RecommendationUtils;
import io.harness.ccm.commons.beans.recommendation.TotalResourceUsage;
import io.harness.ccm.commons.beans.recommendation.models.NodePool;
import io.harness.ccm.commons.beans.recommendation.models.RecommendClusterRequest;
import io.harness.ccm.commons.beans.recommendation.models.RecommendationResponse;
import io.harness.ccm.commons.beans.recommendation.models.VirtualMachine;
import io.harness.ccm.commons.dao.recommendation.K8sRecommendationDAO;
import io.harness.ccm.commons.entities.k8s.recommendation.K8sNodeRecommendation;
import io.harness.ccm.graphql.dto.recommendation.NodeRecommendationDTO;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import lombok.NonNull;

@Singleton
public class NodeRecommendationService {
  @Inject private K8sRecommendationDAO k8sRecommendationDAO;

  public NodeRecommendationDTO getRecommendation(@NonNull String accountIdentifier, @NonNull String id) {
    Optional<K8sNodeRecommendation> k8sNodeRecommendation =
        k8sRecommendationDAO.fetchNodeRecommendationById(accountIdentifier, id);

    if (!k8sNodeRecommendation.isPresent()) {
      return null;
    }

    final K8sNodeRecommendation recommendation = k8sNodeRecommendation.get();

    final NodePool currentNodePool =
        NodePool.builder()
            .sumNodes((long) recommendation.getCurrentServiceProvider().getNodeCount())
            .vm(VirtualMachine.builder()
                    .type(recommendation.getCurrentServiceProvider().getInstanceFamily())
                    .onDemandPrice(recommendation.getCurrentServiceProvider().getCostPerVmPerHr())
                    .avgPrice(recommendation.getCurrentServiceProvider().getSpotCostPerVmPerHr())
                    .cpusPerVm(recommendation.getCurrentServiceProvider().getCpusPerVm())
                    .memPerVm(recommendation.getCurrentServiceProvider().getMemPerVm())
                    .build())
            .build();

    final RecommendationResponse currentConfiguration =
        RecommendationResponse.builder()
            .nodePools(Collections.singletonList(currentNodePool))
            .region(recommendation.getCurrentServiceProvider().getRegion())
            .service(recommendation.getCurrentServiceProvider().getCloudProvider().getK8sService())
            .provider(recommendation.getCurrentServiceProvider().getCloudProvider().getCloudProviderName())
            .instanceCategory(recommendation.getCurrentServiceProvider().getInstanceCategory())
            .build();

    return NodeRecommendationDTO.builder()
        .id(recommendation.getUuid())
        .current(currentConfiguration)
        .recommended(recommendation.getRecommendation())
        .resourceRequirement(recommendation.getRecommendClusterRequest())
        .nodePoolId(recommendation.getNodePoolId())
        .build();
  }

  public RecommendClusterRequest constructRecommendationRequest(@NonNull String accountIdentifier,
      @NonNull NodePoolId nodePoolId, @NonNull OffsetDateTime startTime, @NonNull OffsetDateTime endTime) {
    final TotalResourceUsage totalResourceUsage =
        k8sRecommendationDAO.aggregateTotalResourceRequirement(accountIdentifier, nodePoolId, startTime, endTime);

    return RecommendationUtils.constructNodeRecommendationRequest(totalResourceUsage);
  }
}
