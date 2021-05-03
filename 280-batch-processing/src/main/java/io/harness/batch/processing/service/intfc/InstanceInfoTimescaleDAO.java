package io.harness.batch.processing.service.intfc;

import io.harness.batch.processing.ccm.InstanceEvent;
import io.harness.batch.processing.ccm.InstanceInfo;
import io.harness.event.payloads.Lifecycle;
import io.harness.perpetualtask.k8s.watch.K8sWorkloadSpec;

import java.util.List;
import javax.validation.constraints.NotNull;

public interface InstanceInfoTimescaleDAO {
  void insertIntoNodeInfo(@NotNull InstanceInfo instanceInfo);

  void insertIntoNodeInfo(@NotNull List<InstanceInfo> instanceInfoList);

  void insertIntoWorkloadInfo(@NotNull String accountId, @NotNull K8sWorkloadSpec workloadSpec);

  void insertIntoPodInfo(@NotNull List<InstanceInfo> instanceInfoList);

  void insertIntoPodInfo(@NotNull InstanceInfo instanceInfo);

  void updatePodStopEvent(@NotNull List<InstanceEvent> instanceEventList);

  void updatePodLifecycleEvent(@NotNull String accountId, @NotNull List<Lifecycle> lifecycleList);

  void updateNodeStopEvent(@NotNull List<InstanceEvent> instanceEventList);

  void updateNodeLifecycleEvent(@NotNull String accountId, @NotNull List<Lifecycle> lifecycleList);
}
