package software.wings.graphql.datafetcher;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import software.wings.graphql.schema.type.aggregation.QLData;

import java.util.List;

@OwnedBy(DX)
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public abstract class RealTimeStatsDataFetcher<A, F, G, S>
    extends AbstractStatsDataFetcher<A, F, G, S> implements BaseRealTimeStatsDataFetcher<F> {
  protected QLData getQLData(String accountId, List<F> filters, Class entityClass, List<String> groupByAsStringList) {
    return getQLData(utils, nameService, wingsPersistence, accountId, filters, entityClass, groupByAsStringList);
  }

  @Override
  protected QLData postFetch(String accountId, List<G> groupByList, QLData qlData) {
    return qlData;
  }
}
