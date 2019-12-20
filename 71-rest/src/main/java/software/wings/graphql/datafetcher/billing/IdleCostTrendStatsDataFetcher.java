package software.wings.graphql.datafetcher.billing;

import com.google.inject.Inject;

import io.harness.exception.InvalidRequestException;
import io.harness.timescaledb.DBUtils;
import io.harness.timescaledb.TimeScaleDBService;
import lombok.extern.slf4j.Slf4j;
import software.wings.graphql.datafetcher.AbstractStatsDataFetcherWithAggregationList;
import software.wings.graphql.datafetcher.billing.QLIdleCostData.QLIdleCostDataBuilder;
import software.wings.graphql.schema.type.aggregation.QLData;
import software.wings.graphql.schema.type.aggregation.billing.QLBillingDataFilter;
import software.wings.graphql.schema.type.aggregation.billing.QLBillingSortCriteria;
import software.wings.graphql.schema.type.aggregation.billing.QLBillingStatsInfo;
import software.wings.graphql.schema.type.aggregation.billing.QLCCMGroupBy;
import software.wings.graphql.schema.type.aggregation.billing.QLIdleCostTrendStats;
import software.wings.security.PermissionAttribute.PermissionType;
import software.wings.security.annotations.AuthRule;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import javax.validation.constraints.NotNull;

@Slf4j
public class IdleCostTrendStatsDataFetcher extends AbstractStatsDataFetcherWithAggregationList<QLCCMAggregationFunction,
    QLBillingDataFilter, QLCCMGroupBy, QLBillingSortCriteria> {
  @Inject private TimeScaleDBService timeScaleDBService;
  @Inject BillingDataQueryBuilder billingDataQueryBuilder;

  private static final String TOTAL_IDLE_COST_DESCRIPTION = "%s of total cost";
  private static final String TOTAL_IDLE_COST_LABEL = "Total Idle Cost of %s - %s";
  private static final String TOTAL_IDLE_COST_VALUE = "$%s";
  private static final String CPU_IDLE_COST_DESCRIPTION = "%s avg. utilization";
  private static final String CPU_IDLE_COST_LABEL = "CPU idle Cost";
  private static final String CPU_IDLE_COST_VALUE = "$%s";
  private static final String MEMORY_IDLE_COST_DESCRIPTION = "%s avg. utilization";
  private static final String MEMORY_IDLE_COST_LABEL = "Memory idle Cost";
  private static final String MEMORY_IDLE_COST_VALUE = "$%s";
  private static final String UNALLOCATED_COST_LABEL = "Unallocated Cost";
  private static final String UNALLOCATED_COST_VALUE = "$%s";
  private static final String UNALLOCATED_COST_DESCRIPTION = "%s of total cost";
  private static final String TOTAL_IDLE_COST_DATE_PATTERN = "dd MMMM, yyyy";
  private static final String EMPTY_VALUE = "-";

  @Override
  @AuthRule(permissionType = PermissionType.LOGGED_IN)
  protected QLData fetch(String accountId, List<QLCCMAggregationFunction> aggregateFunction,
      List<QLBillingDataFilter> filters, List<QLCCMGroupBy> groupBy, List<QLBillingSortCriteria> sort) {
    try {
      if (timeScaleDBService.isValid()) {
        return getData(accountId, aggregateFunction, filters);
      } else {
        throw new InvalidRequestException("Cannot process request");
      }
    } catch (Exception e) {
      throw new InvalidRequestException("Error while billing data", e);
    }
  }

  protected QLIdleCostTrendStats getData(
      @NotNull String accountId, List<QLCCMAggregationFunction> aggregateFunction, List<QLBillingDataFilter> filters) {
    QLIdleCostData idleCostData = getIdleCostData(accountId, aggregateFunction, filters);
    // Todo: get unallocated resource cost here
    return QLIdleCostTrendStats.builder()
        .totalIdleCost(getTotalIdleCostStats(idleCostData, filters))
        .cpuIdleCost(getCpuIdleCostStats(idleCostData))
        .memoryIdleCost(getMemoryIdleCostStats(idleCostData))
        .unallocatedCost(getUnallocatedCostStats(BigDecimal.TEN, idleCostData))
        .build();
  }

  protected QLIdleCostData getIdleCostData(
      String accountId, List<QLCCMAggregationFunction> aggregateFunction, List<QLBillingDataFilter> filters) {
    BillingDataQueryMetadata queryData =
        billingDataQueryBuilder.formTrendStatsQuery(accountId, aggregateFunction, filters);
    String query = queryData.getQuery();
    logger.info("Idle cost data query {}", query);
    ResultSet resultSet = null;
    try (Connection connection = timeScaleDBService.getDBConnection();
         Statement statement = connection.createStatement()) {
      resultSet = statement.executeQuery(query);
      return fetchIdleCostStats(queryData, resultSet);
    } catch (SQLException e) {
      throw new InvalidRequestException("IdleCostDataFetcher Exception ", e);
    } finally {
      DBUtils.close(resultSet);
    }
  }

  private QLIdleCostData fetchIdleCostStats(BillingDataQueryMetadata queryData, ResultSet resultSet)
      throws SQLException {
    QLIdleCostDataBuilder idleCostDataBuilder = QLIdleCostData.builder();
    while (null != resultSet && resultSet.next()) {
      queryData.getFieldNames().forEach(field -> {
        try {
          switch (field) {
            case IDLECOST:
              idleCostDataBuilder.idleCost(resultSet.getBigDecimal(field.getFieldName()));
              break;
            case MIN_STARTTIME:
              idleCostDataBuilder.minStartTime(
                  resultSet.getTimestamp(field.getFieldName(), utils.getDefaultCalendar()).getTime());
              break;
            case MAX_STARTTIME:
              idleCostDataBuilder.maxStartTime(
                  resultSet.getTimestamp(field.getFieldName(), utils.getDefaultCalendar()).getTime());
              break;
            case SUM:
              idleCostDataBuilder.totalCost(resultSet.getBigDecimal(field.getFieldName()));
              break;
            case CPUIDLECOST:
              idleCostDataBuilder.cpuIdleCost(resultSet.getBigDecimal(field.getFieldName()));
              break;
            case MEMORYIDLECOST:
              idleCostDataBuilder.memoryIdleCost(resultSet.getBigDecimal(field.getFieldName()));
              break;
            case AVGCPUUTILIZATION:
              idleCostDataBuilder.avgCpuUtilization(resultSet.getBigDecimal(field.getFieldName()));
              break;
            case AVGMEMORYUTILIZATION:
              idleCostDataBuilder.avgMemoryUtilization(resultSet.getBigDecimal(field.getFieldName()));
              break;
            default:
              break;
          }
        } catch (Exception e) {
          throw new InvalidRequestException("Error in reading from result set : ", e);
        }
      });
    }
    return idleCostDataBuilder.build();
  }

  private QLBillingStatsInfo getTotalIdleCostStats(QLIdleCostData idleCostData, List<QLBillingDataFilter> filters) {
    Instant startInstant =
        Instant.ofEpochMilli(billingDataQueryBuilder.getStartTimeFilter(filters).getValue().longValue());
    Instant endInstant = Instant.ofEpochMilli(idleCostData.getMaxStartTime());
    String startInstantFormat = getTotalIdleCostFormattedDate(startInstant);
    String endInstantFormat = getTotalIdleCostFormattedDate(endInstant);
    String totalCostLabel = String.format(TOTAL_IDLE_COST_LABEL, startInstantFormat, endInstantFormat);
    String totalCostDescription = EMPTY_VALUE;
    String totalCostValue = EMPTY_VALUE;
    if (idleCostData.getIdleCost() != null) {
      totalCostValue = String.format(
          TOTAL_IDLE_COST_VALUE, billingDataQueryBuilder.getRoundedDoubleValue(idleCostData.getIdleCost()));
      if (idleCostData.getTotalCost() != null) {
        double percentageOfTotalCost = billingDataQueryBuilder.getRoundedDoubleValue(BigDecimal.valueOf(
            100 * idleCostData.getIdleCost().doubleValue() / idleCostData.getTotalCost().doubleValue()));
        totalCostDescription = String.format(TOTAL_IDLE_COST_DESCRIPTION, percentageOfTotalCost + "%");
      }
    }
    return QLBillingStatsInfo.builder()
        .statsLabel(totalCostLabel)
        .statsDescription(totalCostDescription)
        .statsValue(totalCostValue)
        .build();
  }

  private QLBillingStatsInfo getCpuIdleCostStats(QLIdleCostData idleCostData) {
    String cpuIdleCostDescription = EMPTY_VALUE;
    String cpuIdleCostValue = EMPTY_VALUE;
    if (idleCostData.getAvgCpuUtilization() != null) {
      cpuIdleCostDescription = String.format(CPU_IDLE_COST_DESCRIPTION,
          100 * billingDataQueryBuilder.getRoundedDoubleValue(idleCostData.getAvgCpuUtilization()) + "%");
    }
    if (idleCostData.getCpuIdleCost() != null) {
      cpuIdleCostValue = String.format(
          CPU_IDLE_COST_VALUE, billingDataQueryBuilder.getRoundedDoubleValue(idleCostData.getCpuIdleCost()));
    }
    return QLBillingStatsInfo.builder()
        .statsLabel(CPU_IDLE_COST_LABEL)
        .statsDescription(cpuIdleCostDescription)
        .statsValue(cpuIdleCostValue)
        .build();
  }

  private QLBillingStatsInfo getMemoryIdleCostStats(QLIdleCostData idleCostData) {
    String memoryIdleCostDescription = EMPTY_VALUE;
    String memoryIdleCostValue = EMPTY_VALUE;
    if (idleCostData.getAvgMemoryUtilization() != null) {
      memoryIdleCostDescription = String.format(MEMORY_IDLE_COST_DESCRIPTION,
          100 * billingDataQueryBuilder.getRoundedDoubleValue(idleCostData.getAvgMemoryUtilization()) + "%");
    }
    if (idleCostData.getMemoryIdleCost() != null) {
      memoryIdleCostValue = String.format(
          MEMORY_IDLE_COST_VALUE, billingDataQueryBuilder.getRoundedDoubleValue(idleCostData.getMemoryIdleCost()));
    }
    return QLBillingStatsInfo.builder()
        .statsLabel(MEMORY_IDLE_COST_LABEL)
        .statsDescription(memoryIdleCostDescription)
        .statsValue(memoryIdleCostValue)
        .build();
  }

  private QLBillingStatsInfo getUnallocatedCostStats(BigDecimal unallocatedCost, QLIdleCostData idleCostData) {
    String unallocatedCostValue = EMPTY_VALUE;
    String unallocatedCostDescription = EMPTY_VALUE;
    if (unallocatedCost != null) {
      unallocatedCostValue =
          String.format(UNALLOCATED_COST_VALUE, billingDataQueryBuilder.getRoundedDoubleValue(unallocatedCost));
      if (idleCostData.getTotalCost() != null) {
        double percentageOfTotalCost = billingDataQueryBuilder.getRoundedDoubleValue(
            BigDecimal.valueOf(100 * unallocatedCost.doubleValue() / idleCostData.getTotalCost().doubleValue()));
        unallocatedCostDescription = String.format(UNALLOCATED_COST_DESCRIPTION, percentageOfTotalCost + "%");
      }
    }
    return QLBillingStatsInfo.builder()
        .statsLabel(UNALLOCATED_COST_LABEL)
        .statsDescription(unallocatedCostDescription)
        .statsValue(unallocatedCostValue)
        .build();
  }

  private String getTotalIdleCostFormattedDate(Instant instant) {
    return billingDataQueryBuilder.getFormattedDate(instant, TOTAL_IDLE_COST_DATE_PATTERN);
  }

  @Override
  protected QLData postFetch(String accountId, List<QLCCMGroupBy> groupByList, QLData qlData) {
    return null;
  }

  @Override
  public String getEntityType() {
    return null;
  }
}
