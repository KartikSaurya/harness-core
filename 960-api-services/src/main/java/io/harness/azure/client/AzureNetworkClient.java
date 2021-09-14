/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.azure.client;

import io.harness.azure.model.AzureConfig;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.LoadBalancingRule;
import java.util.List;
import java.util.Optional;

public interface AzureNetworkClient {
  /**
   * Get Load Balancer by name.
   *
   * @param azureConfig
   * @param subscriptionId
   * @param resourceGroupName
   * @param loadBalancerName
   * @return
   */
  Optional<LoadBalancer> getLoadBalancerByName(
      AzureConfig azureConfig, String subscriptionId, String resourceGroupName, String loadBalancerName);

  /**
   * List load balancers by resource group.
   *
   * @param azureConfig
   * @param subscriptionId
   * @param resourceGroupName
   * @return
   */
  List<LoadBalancer> listLoadBalancersByResourceGroup(
      AzureConfig azureConfig, String subscriptionId, String resourceGroupName);

  /**
   * List load balancer backend pools.
   *
   * @param azureConfig
   * @param subscriptionId
   * @param resourceGroupName
   * @param loadBalancerName
   * @return
   */
  List<LoadBalancerBackend> listLoadBalancerBackendPools(
      AzureConfig azureConfig, String subscriptionId, String resourceGroupName, String loadBalancerName);

  /**
   * List load balancer TCP probes.
   *
   * @param azureConfig
   * @param subscriptionId
   * @param resourceGroupName
   * @param loadBalancerName
   * @return
   */
  List<LoadBalancerTcpProbe> listLoadBalancerTcpProbes(
      AzureConfig azureConfig, String subscriptionId, String resourceGroupName, String loadBalancerName);

  /**
   * List backend pool rules.
   *
   * @param azureConfig
   * @param subscriptionId
   * @param resourceGroupName
   * @param loadBalancerName
   * @param backendPoolName
   * @return
   */
  List<LoadBalancingRule> listBackendPoolRules(AzureConfig azureConfig, String subscriptionId, String resourceGroupName,
      String loadBalancerName, String backendPoolName);

  /**
   * List backend pool probes.
   *
   * @param azureConfig
   * @param subscriptionId
   * @param resourceGroupName
   * @param loadBalancerName
   * @param backendPoolName
   * @return
   */
  List<LoadBalancerProbe> listBackendPoolProbes(AzureConfig azureConfig, String subscriptionId,
      String resourceGroupName, String loadBalancerName, String backendPoolName);
}
