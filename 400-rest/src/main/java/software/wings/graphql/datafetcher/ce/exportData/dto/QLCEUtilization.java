package software.wings.graphql.datafetcher.ce.exportData.dto;

import static io.harness.annotations.dev.HarnessTeam.CE;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
@TargetModule(HarnessModule._375_CE_GRAPHQL)
@OwnedBy(CE)
public enum QLCEUtilization {
  CPU_REQUEST,
  CPU_LIMIT,
  CPU_UTILIZATION_VALUE,
  MEMORY_REQUEST,
  MEMORY_LIMIT,
  MEMORY_UTILIZATION_VALUE
}
