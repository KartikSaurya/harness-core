package io.harness.ccm.cluster.entities;

import static io.harness.ccm.cluster.entities.ClusterType.GCP_KUBERNETES;

import io.harness.ccm.cluster.entities.ClusterRecord.ClusterRecordKeys;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.helpers.ext.k8s.request.K8sClusterConfig;
import software.wings.settings.SettingValue;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.query.Query;

@Data
@JsonTypeName("GCP_KUBERNETES")
@FieldDefaults(level = AccessLevel.PRIVATE)
@FieldNameConstants(innerTypeName = "GcpKubernetesClusterKeys")
public class GcpKubernetesCluster implements Cluster, KubernetesCluster {
  final String cloudProviderId;
  final String clusterName;

  public static final String cloudProviderField =
      ClusterRecordKeys.cluster + "." + GcpKubernetesClusterKeys.cloudProviderId;
  public static final String clusterNameField = ClusterRecordKeys.cluster + "." + GcpKubernetesClusterKeys.clusterName;

  @Builder
  public GcpKubernetesCluster(String cloudProviderId, String clusterName) {
    this.cloudProviderId = cloudProviderId;
    this.clusterName = clusterName;
  }

  @Override
  public String getClusterType() {
    return GCP_KUBERNETES;
  }

  @Override
  public void addRequiredQueryFilters(Query<ClusterRecord> query) {
    query.field(cloudProviderField)
        .equal(this.getCloudProviderId())
        .field(clusterNameField)
        .equal(this.getClusterName());
  }

  @Override
  public K8sClusterConfig toK8sClusterConfig(SettingValue cloudProvider, List<EncryptedDataDetail> encryptionDetails) {
    return K8sClusterConfig.builder()
        .cloudProvider(cloudProvider)
        .cloudProviderEncryptionDetails(encryptionDetails)
        .gcpKubernetesCluster(software.wings.beans.GcpKubernetesCluster.builder().clusterName(clusterName).build())
        .build();
  }
}
