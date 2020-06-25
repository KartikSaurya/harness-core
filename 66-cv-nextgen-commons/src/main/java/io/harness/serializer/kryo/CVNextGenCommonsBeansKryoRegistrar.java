package io.harness.serializer.kryo;

import static io.harness.annotations.dev.HarnessTeam.CV;

import com.esotericsoftware.kryo.Kryo;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cvng.beans.AppDynamicsDataCollectionInfo;
import io.harness.cvng.beans.DataSourceType;
import io.harness.cvng.beans.TimeSeriesMetricType;
import io.harness.cvng.core.services.entities.AppDynamicsCVConfig;
import io.harness.cvng.core.services.entities.CVConfig;
import io.harness.cvng.core.services.entities.MetricCVConfig;
import io.harness.cvng.core.services.entities.MetricPack;
import io.harness.cvng.core.services.entities.MetricPack.MetricDefinition;
import io.harness.cvng.models.VerificationType;
import io.harness.serializer.KryoRegistrar;

@OwnedBy(CV)
public class CVNextGenCommonsBeansKryoRegistrar implements KryoRegistrar {
  @Override
  public void register(Kryo kryo) {
    kryo.register(MetricPack.class, 9000);
    kryo.register(MetricDefinition.class, 9001);
    kryo.register(DataSourceType.class, 9002);
    kryo.register(TimeSeriesMetricType.class, 9003);
    kryo.register(CVConfig.class, 9004);
    kryo.register(MetricCVConfig.class, 9005);
    kryo.register(AppDynamicsCVConfig.class, 9006);
    kryo.register(AppDynamicsDataCollectionInfo.class, 9007);
    kryo.register(VerificationType.class, 9008);
  }
}
