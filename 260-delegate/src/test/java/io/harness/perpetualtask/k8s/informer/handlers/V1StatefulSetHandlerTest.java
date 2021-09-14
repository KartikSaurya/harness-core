/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.perpetualtask.k8s.informer.handlers;

import static io.harness.rule.OwnerRule.AVMOHAN;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.event.client.EventPublisher;
import io.harness.perpetualtask.k8s.informer.ClusterDetails;
import io.harness.perpetualtask.k8s.watch.K8sWorkloadSpec;
import io.harness.rule.Owner;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetBuilder;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;

public class V1StatefulSetHandlerTest extends CategoryTest {
  private static final Integer VERSION = 1;
  private EventPublisher eventPublisher;
  private final ClusterDetails clusterDetails = ClusterDetails.builder()
                                                    .clusterName("ce-test-cluster")
                                                    .cloudProviderId("cloud-provider-id")
                                                    .clusterId("cluster-id")
                                                    .kubeSystemUid("kube-system-uid")
                                                    .build();
  private V1StatefulSetHandler statefulSetHandler;
  private ArgumentCaptor<Message> captor;

  @Before
  public void setUp() throws Exception {
    eventPublisher = mock(EventPublisher.class);
    statefulSetHandler = new V1StatefulSetHandler(eventPublisher, clusterDetails);
    captor = ArgumentCaptor.forClass(Message.class);
  }

  @Test
  @Owner(developers = AVMOHAN)
  @Category(UnitTests.class)
  public void shouldPublishWorkloadSpecOnAdd() throws Exception {
    V1StatefulSet statefulSet = new V1StatefulSetBuilder()
                                    .withNewMetadata()
                                    .withName("test-name")
                                    .withNamespace("test-namespace")
                                    .withUid("test-uid")
                                    .endMetadata()
                                    .withNewSpec()
                                    .withReplicas(5)
                                    .withNewTemplate()
                                    .withNewSpec()
                                    .addNewInitContainer()
                                    .withName("init-container-1")
                                    .endInitContainer()
                                    .addNewContainer()
                                    .withName("container-1")
                                    .withNewResources()
                                    .addToRequests("cpu", Quantity.fromString("100m"))
                                    .addToRequests("memory", Quantity.fromString("100Mi"))
                                    .addToLimits("cpu", Quantity.fromString("200m"))
                                    .addToLimits("memory", Quantity.fromString("200Mi"))
                                    .endResources()
                                    .endContainer()
                                    .addNewContainer()
                                    .withName("container-2")
                                    .withNewResources()
                                    .addToRequests("cpu", Quantity.fromString("750m"))
                                    .addToRequests("memory", Quantity.fromString("1Gi"))
                                    .addToLimits("cpu", Quantity.fromString("1500m"))
                                    .addToLimits("memory", Quantity.fromString("2Gi"))
                                    .endResources()
                                    .endContainer()
                                    .endSpec()
                                    .endTemplate()
                                    .endSpec()
                                    .build();
    statefulSetHandler.onAdd(statefulSet);
    assertThat(getPublishedMessages())
        .contains(K8sWorkloadSpec.newBuilder()
                      .setNamespace("test-namespace")
                      .setWorkloadName("test-name")
                      .setUid("test-uid")
                      .setVersion(VERSION)
                      .setReplicas(5)
                      .setClusterName("ce-test-cluster")
                      .setClusterId("cluster-id")
                      .setKubeSystemUid("kube-system-uid")
                      .setCloudProviderId("cloud-provider-id")
                      .setWorkloadKind("StatefulSet")
                      .addAllContainerSpecs(ImmutableList.of(K8sWorkloadSpec.ContainerSpec.newBuilder()
                                                                 .setName("container-1")
                                                                 .putRequests("cpu", "100m")
                                                                 .putRequests("memory", "100Mi")
                                                                 .putLimits("cpu", "200m")
                                                                 .putLimits("memory", "200Mi")
                                                                 .build(),
                          K8sWorkloadSpec.ContainerSpec.newBuilder()
                              .setName("container-2")
                              .putRequests("cpu", "750m")
                              .putRequests("memory", "1Gi")
                              .putLimits("cpu", "1500m")
                              .putLimits("memory", "2Gi")
                              .build()))
                      .addAllInitContainerSpecs(ImmutableList.of(
                          K8sWorkloadSpec.ContainerSpec.newBuilder().setName("init-container-1").build()))
                      .build());
  }

  @Test
  @Owner(developers = AVMOHAN)
  @Category(UnitTests.class)
  public void shouldPublishWorkloadSpecOnUpdateIfChangeMade() throws Exception {
    V1StatefulSet oldStatefulSet = new V1StatefulSetBuilder()
                                       .withNewMetadata()
                                       .withName("test-name")
                                       .withNamespace("test-namespace")
                                       .withUid("test-uid")
                                       .endMetadata()
                                       .withNewSpec()
                                       .withReplicas(5)
                                       .withNewTemplate()
                                       .withNewSpec()
                                       .addNewInitContainer()
                                       .withName("init-container-1")
                                       .endInitContainer()
                                       .addNewContainer()
                                       .withName("container-1")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("100m"))
                                       .addToRequests("memory", Quantity.fromString("100Mi"))
                                       .addToLimits("cpu", Quantity.fromString("200m"))
                                       .addToLimits("memory", Quantity.fromString("200Mi"))
                                       .endResources()
                                       .endContainer()
                                       .addNewContainer()
                                       .withName("container-2")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("750m"))
                                       .addToRequests("memory", Quantity.fromString("1Gi"))
                                       .addToLimits("cpu", Quantity.fromString("1500m"))
                                       .addToLimits("memory", Quantity.fromString("2Gi"))
                                       .endResources()
                                       .endContainer()
                                       .endSpec()
                                       .endTemplate()
                                       .endSpec()
                                       .build();
    V1StatefulSet newStatefulSet = new V1StatefulSetBuilder(oldStatefulSet)
                                       .withNewSpec()
                                       .withReplicas(12)
                                       .withNewTemplate()
                                       .withNewSpec()
                                       .addNewInitContainer()
                                       .withName("init-container-1")
                                       .withNewResources()
                                       .addToRequests("memory", Quantity.fromString("1Gi"))
                                       .endResources()
                                       .endInitContainer()
                                       .addNewContainer()
                                       .withName("container-1")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("100m"))
                                       .addToRequests("memory", Quantity.fromString("100Mi"))
                                       .addToLimits("cpu", Quantity.fromString("400m"))
                                       .addToLimits("memory", Quantity.fromString("400Mi"))
                                       .endResources()
                                       .endContainer()
                                       .addNewContainer()
                                       .withName("container-2")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("750m"))
                                       .addToRequests("memory", Quantity.fromString("1Gi"))
                                       .addToLimits("cpu", Quantity.fromString("3000m"))
                                       .addToLimits("memory", Quantity.fromString("4Gi"))
                                       .endResources()
                                       .endContainer()
                                       .endSpec()
                                       .endTemplate()
                                       .endSpec()
                                       .build();
    statefulSetHandler.onUpdate(oldStatefulSet, newStatefulSet);
    assertThat(getPublishedMessages().get(0))
        .isEqualTo(K8sWorkloadSpec.newBuilder()
                       .setNamespace("test-namespace")
                       .setWorkloadName("test-name")
                       .setUid("test-uid")
                       .setVersion(VERSION)
                       .setReplicas(12)
                       .setClusterName("ce-test-cluster")
                       .setClusterId("cluster-id")
                       .setKubeSystemUid("kube-system-uid")
                       .setCloudProviderId("cloud-provider-id")
                       .setWorkloadKind("StatefulSet")
                       .addAllInitContainerSpecs(ImmutableList.of(K8sWorkloadSpec.ContainerSpec.newBuilder()
                                                                      .setName("init-container-1")
                                                                      .putRequests("memory", "1Gi")
                                                                      .build()))
                       .addAllContainerSpecs(ImmutableList.of(K8sWorkloadSpec.ContainerSpec.newBuilder()
                                                                  .setName("container-1")
                                                                  .putRequests("cpu", "100m")
                                                                  .putRequests("memory", "100Mi")
                                                                  .putLimits("cpu", "400m")
                                                                  .putLimits("memory", "400Mi")
                                                                  .build(),
                           K8sWorkloadSpec.ContainerSpec.newBuilder()
                               .setName("container-2")
                               .putRequests("cpu", "750m")
                               .putRequests("memory", "1Gi")
                               .putLimits("cpu", "3")
                               .putLimits("memory", "4Gi")
                               .build()))
                       .build());
  }

  @Test
  @Owner(developers = AVMOHAN)
  @Category(UnitTests.class)
  public void shouldNotPublishWorkloadSpecOnUpdateIfNoChangeMade() throws Exception {
    V1StatefulSet oldStatefulSet = new V1StatefulSetBuilder()
                                       .withNewMetadata()
                                       .withName("test-name")
                                       .withNamespace("test-namespace")
                                       .withUid("test-uid")
                                       .endMetadata()
                                       .withNewSpec()
                                       .withReplicas(5)
                                       .withNewTemplate()
                                       .withNewSpec()
                                       .addNewContainer()
                                       .withName("container-1")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("100m"))
                                       .addToRequests("memory", Quantity.fromString("100Mi"))
                                       .addToLimits("cpu", Quantity.fromString("200m"))
                                       .addToLimits("memory", Quantity.fromString("200Mi"))
                                       .endResources()
                                       .endContainer()
                                       .addNewContainer()
                                       .withName("container-2")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("750m"))
                                       .addToRequests("memory", Quantity.fromString("1Gi"))
                                       .addToLimits("cpu", Quantity.fromString("1500m"))
                                       .addToLimits("memory", Quantity.fromString("2Gi"))
                                       .endResources()
                                       .endContainer()
                                       .endSpec()
                                       .endTemplate()
                                       .endSpec()
                                       .build();
    V1StatefulSet newStatefulSet = new V1StatefulSetBuilder(oldStatefulSet)
                                       .withNewSpec()
                                       .withReplicas(5)
                                       .withNewTemplate()
                                       .withNewSpec()
                                       .withAutomountServiceAccountToken(false)
                                       .addNewContainer()
                                       .withName("container-1")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("100m"))
                                       .addToRequests("memory", Quantity.fromString("100Mi"))
                                       .addToLimits("cpu", Quantity.fromString("200m"))
                                       .addToLimits("memory", Quantity.fromString("200Mi"))
                                       .endResources()
                                       .endContainer()
                                       .addNewContainer()
                                       .withName("container-2")
                                       .withNewResources()
                                       .addToRequests("cpu", Quantity.fromString("750m"))
                                       .addToRequests("memory", Quantity.fromString("1Gi"))
                                       .addToLimits("cpu", Quantity.fromString("1500m"))
                                       .addToLimits("memory", Quantity.fromString("2Gi"))
                                       .endResources()
                                       .endContainer()
                                       .endSpec()
                                       .endTemplate()
                                       .endSpec()
                                       .build();
    statefulSetHandler.onUpdate(oldStatefulSet, newStatefulSet);
    assertThat(getPublishedMessages()).isEmpty();
  }

  private List<Message> getPublishedMessages() {
    verify(eventPublisher, atLeastOnce()).publishMessage(captor.capture(), any(), any());
    return captor.getAllValues().stream().filter(x -> x instanceof K8sWorkloadSpec).collect(Collectors.toList());
  }
}
