package software.wings.metrics;

import static org.junit.Assert.assertEquals;
import static software.wings.common.VerificationConstants.HEARTBEAT_METRIC_NAME;

import com.google.common.collect.TreeBasedTable;

import io.fabric8.utils.Lists;
import io.harness.category.element.UnitTests;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import software.wings.service.impl.newrelic.NewRelicMetricDataRecord;
import software.wings.service.intfc.analysis.ClusterLevel;
import software.wings.sm.StateType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeSeriesDataRecordTest {
  String stateExecutionId = "stateExecutionId";
  String workflowExecutionId = "workflowExecutionId";
  String workflowId = "workflowId";
  String serviceId = "serviceId";
  StateType stateType = StateType.APP_DYNAMICS;

  private NewRelicMetricDataRecord getNewRelicInputForTest(long timestamp, int dataCollectionMinute, String host,
      ClusterLevel level, String transactionName, List<String> metricNames) {
    Map<String, Double> values = new HashMap<>();
    metricNames.forEach(name -> values.put(name, 3.4));
    return NewRelicMetricDataRecord.builder()
        .stateType(stateType)
        .workflowId(workflowId)
        .workflowExecutionId(workflowExecutionId)
        .serviceId(serviceId)
        .stateExecutionId(stateExecutionId)
        .timeStamp(timestamp)
        .dataCollectionMinute(dataCollectionMinute)
        .host(host)
        .level(level)
        .name(transactionName)
        .values(values)
        .build();
  }

  private TimeSeriesDataRecord getTimeSeriesInputForTest(long timestamp, int dataCollectionMinute, String host,
      ClusterLevel level, Map<String, List<String>> metricNames) {
    TreeBasedTable<String, String, Double> values = TreeBasedTable.create();
    metricNames.forEach((key, list) -> list.forEach(value -> values.put(key, value, 3.4)));
    return TimeSeriesDataRecord.builder()
        .stateType(stateType)
        .workflowId(workflowId)
        .workflowExecutionId(workflowExecutionId)
        .serviceId(serviceId)
        .stateExecutionId(stateExecutionId)
        .timeStamp(timestamp)
        .dataCollectionMinute(dataCollectionMinute)
        .host(host)
        .level(level)
        .values(values)
        .build();
  }

  @Test
  @Category(UnitTests.class)
  public void getTimeSeriesDataRecordsFromNewRelicDataRecords() {
    DateTime now = DateTime.now();
    String host1 = "host";
    String host2 = "host_new";
    String transaction1 = "/get-metrics";
    String transaction2 = "/get-transactions";
    String metric1 = "response-time";
    String metric2 = "throughput";
    List<String> metrics = Lists.newArrayList(metric1, metric2);
    List<NewRelicMetricDataRecord> inputRecords = Lists.newArrayList(
        getNewRelicInputForTest(now.getMillis(), 0, host1, null, transaction1, metrics),
        getNewRelicInputForTest(now.getMillis(), 0, host1, null, transaction2, metrics),
        getNewRelicInputForTest(now.getMillis(), 0, host2, null, transaction1, metrics),
        getNewRelicInputForTest(now.getMillis(), 0, host2, null, transaction2, metrics),
        getNewRelicInputForTest(now.getMillis(), 0, null, ClusterLevel.H0, HEARTBEAT_METRIC_NAME, new ArrayList<>()),
        getNewRelicInputForTest(now.getMillis(), 1, host1, null, transaction1, metrics),
        getNewRelicInputForTest(now.getMillis(), 1, host1, null, transaction2, metrics),
        getNewRelicInputForTest(now.getMillis(), 1, host2, null, transaction1, metrics),
        getNewRelicInputForTest(now.getMillis(), 1, host2, null, transaction2, metrics),
        getNewRelicInputForTest(now.getMillis(), 1, null, ClusterLevel.H0, HEARTBEAT_METRIC_NAME, new ArrayList<>()));

    List<TimeSeriesDataRecord> dataRecords =
        TimeSeriesDataRecord.getTimeSeriesDataRecordsFromNewRelicDataRecords(inputRecords);
    Map<String, List<String>> transactionMap = new HashMap<>();
    transactionMap.put(transaction1, metrics);
    transactionMap.put(transaction2, metrics);
    List<TimeSeriesDataRecord> expectedOutput =
        Lists.newArrayList(getTimeSeriesInputForTest(now.getMillis(), 0, host1, null, transactionMap),
            getTimeSeriesInputForTest(now.getMillis(), 0, host2, null, transactionMap),
            getTimeSeriesInputForTest(now.getMillis(), 0, null, ClusterLevel.H0, new HashMap<>()),
            getTimeSeriesInputForTest(now.getMillis(), 1, host1, null, transactionMap),
            getTimeSeriesInputForTest(now.getMillis(), 1, host2, null, transactionMap),
            getTimeSeriesInputForTest(now.getMillis(), 1, null, ClusterLevel.H0, new HashMap<>()));

    assertEquals(6, dataRecords.size());

    expectedOutput.sort(
        Comparator.comparing(TimeSeriesDataRecord::getDataCollectionMinute)
            .thenComparing(TimeSeriesDataRecord::getHost, Comparator.nullsFirst(Comparator.naturalOrder())));
    dataRecords.sort(
        Comparator.comparing(TimeSeriesDataRecord::getDataCollectionMinute)
            .thenComparing(TimeSeriesDataRecord::getHost, Comparator.nullsFirst(Comparator.naturalOrder())));
    assertEquals(expectedOutput, dataRecords);
  }
}