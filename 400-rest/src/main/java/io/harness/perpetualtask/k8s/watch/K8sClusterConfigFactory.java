/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.perpetualtask.k8s.watch;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ccm.cluster.ClusterRecordService;
import io.harness.ccm.cluster.entities.ClusterRecord;
import io.harness.ccm.cluster.entities.KubernetesCluster;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.annotation.EncryptableSetting;
import software.wings.beans.SettingAttribute;
import software.wings.helpers.ext.k8s.request.K8sClusterConfig;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.security.SecretManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@OwnedBy(HarnessTeam.CDP)
@Singleton
public class K8sClusterConfigFactory {
  private final SecretManager secretManager;
  private final SettingsService settingsService;
  private final ClusterRecordService clusterRecordService;

  @Inject
  K8sClusterConfigFactory(
      SecretManager secretManager, SettingsService settingsService, ClusterRecordService clusterRecordService) {
    this.secretManager = secretManager;
    this.settingsService = settingsService;
    this.clusterRecordService = clusterRecordService;
  }

  public K8sClusterConfig getK8sClusterConfig(String clusterId) {
    ClusterRecord clusterRecord = clusterRecordService.get(clusterId);
    SettingAttribute settingAttribute = settingsService.get(clusterRecord.getCluster().getCloudProviderId());
    EncryptableSetting encryptableSetting = (EncryptableSetting) settingAttribute.getValue();
    List<EncryptedDataDetail> encryptionDetails = secretManager.getEncryptionDetails(encryptableSetting);
    KubernetesCluster kubernetesCluster = (KubernetesCluster) clusterRecord.getCluster();
    return kubernetesCluster.toK8sClusterConfig(settingAttribute.getValue(), encryptionDetails);
  }
}
