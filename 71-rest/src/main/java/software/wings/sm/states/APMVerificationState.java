package software.wings.sm.states;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static software.wings.common.VerificationConstants.VERIFICATION_HOST_PLACEHOLDER;
import static software.wings.service.impl.apm.APMMetricInfo.ResponseMapper;
import static software.wings.service.impl.newrelic.NewRelicMetricDataRecord.DEFAULT_GROUP_NAME;
import static software.wings.sm.states.DynatraceState.CONTROL_HOST_NAME;
import static software.wings.sm.states.DynatraceState.TEST_HOST_NAME;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import io.harness.beans.DelegateTask;
import io.harness.context.ContextElementType;
import io.harness.delegate.beans.TaskData;
import io.harness.eraro.ErrorCode;
import io.harness.exception.VerificationOperationException;
import io.harness.exception.WingsException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import software.wings.beans.APMVerificationConfig;
import software.wings.beans.SettingAttribute;
import software.wings.beans.TaskType;
import software.wings.beans.TemplateExpression;
import software.wings.metrics.MetricType;
import software.wings.metrics.TimeSeriesMetricDefinition;
import software.wings.service.impl.analysis.AnalysisComparisonStrategy;
import software.wings.service.impl.analysis.AnalysisComparisonStrategyProvider;
import software.wings.service.impl.analysis.AnalysisContext;
import software.wings.service.impl.analysis.AnalysisTolerance;
import software.wings.service.impl.analysis.AnalysisToleranceProvider;
import software.wings.service.impl.analysis.DataCollectionCallback;
import software.wings.service.impl.analysis.TimeSeriesMetricGroup;
import software.wings.service.impl.analysis.TimeSeriesMlAnalysisType;
import software.wings.service.impl.apm.APMDataCollectionInfo;
import software.wings.service.impl.apm.APMMetricInfo;
import software.wings.sm.ExecutionContext;
import software.wings.sm.StateType;
import software.wings.sm.WorkflowStandardParams;
import software.wings.stencils.DefaultValue;
import software.wings.stencils.EnumData;
import software.wings.verification.VerificationStateAnalysisExecutionData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class APMVerificationState extends AbstractMetricAnalysisState {
  @SchemaIgnore protected static final String URL_BODY_APPENDER = "__harness-body__";

  public APMVerificationState(String name) {
    super(name, StateType.APM_VERIFICATION);
  }

  @Attributes(required = true, title = "APM Server") private String analysisServerConfigId;

  private List<MetricCollectionInfo> metricCollectionInfos;

  public void setMetricCollectionInfos(List<MetricCollectionInfo> metricCollectionInfos) {
    this.metricCollectionInfos = metricCollectionInfos;
  }

  @Attributes(required = false, title = "APM DataCollection Rate (mins)") private int dataCollectionRate;

  public int getDataCollectionRate() {
    return dataCollectionRate < 1 ? 1 : dataCollectionRate;
  }

  public void setDataCollectionRate(int dataCollectionRate) {
    this.dataCollectionRate = dataCollectionRate;
  }

  @Attributes(title = "Expression for Host/Container name")
  @DefaultValue("")
  public String getHostnameTemplate() {
    return hostnameTemplate;
  }

  public void setHostnameTemplate(String hostnameTemplate) {
    this.hostnameTemplate = hostnameTemplate;
  }

  @Attributes(required = true, title = "Include nodes from previous phases")
  public boolean getIncludePreviousPhaseNodes() {
    return includePreviousPhaseNodes;
  }

  public void setIncludePreviousPhaseNodes(boolean includePreviousPhaseNodes) {
    this.includePreviousPhaseNodes = includePreviousPhaseNodes;
  }

  @EnumData(enumDataProvider = AnalysisComparisonStrategyProvider.class)
  @Attributes(required = true, title = "Baseline for Risk Analysis")
  @DefaultValue("COMPARE_WITH_PREVIOUS")
  public AnalysisComparisonStrategy getComparisonStrategy() {
    if (isBlank(comparisonStrategy)) {
      return AnalysisComparisonStrategy.COMPARE_WITH_PREVIOUS;
    }
    return AnalysisComparisonStrategy.valueOf(comparisonStrategy);
  }

  @Attributes(title = "Analysis Time duration (in minutes)", description = "Default 15 minutes")
  @DefaultValue("15")
  public String getTimeDuration() {
    if (isBlank(timeDuration)) {
      return String.valueOf(15);
    }
    return timeDuration;
  }

  @EnumData(enumDataProvider = AnalysisToleranceProvider.class)
  @Attributes(required = true, title = "Algorithm Sensitivity")
  @DefaultValue("MEDIUM")
  public AnalysisTolerance getAnalysisTolerance() {
    if (isBlank(tolerance)) {
      return AnalysisTolerance.LOW;
    }
    return AnalysisTolerance.valueOf(tolerance);
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  @Override
  public String getAnalysisServerConfigId() {
    return analysisServerConfigId;
  }

  @Override
  public void setAnalysisServerConfigId(String analysisServerConfigId) {
    this.analysisServerConfigId = analysisServerConfigId;
  }

  @Override
  public Map<String, String> validateFields() {
    Map<String, String> invalidFields = new HashMap<>();
    if (isEmpty(metricCollectionInfos)) {
      invalidFields.put("Metric Collection Info", "Metric collection info should not be empty");
      return invalidFields;
    }
    AtomicBoolean hasBaselineUrl = new AtomicBoolean(false);
    metricCollectionInfos.forEach(metricCollectionInfo -> {
      if (isEmpty(metricCollectionInfo.getMetricName())) {
        invalidFields.put("metricName", "MetricName is empty");
        return;
      }

      if (isEmpty(metricCollectionInfo.getCollectionUrl())) {
        invalidFields.put(
            "collectionUrl", "Metric Collection URL is empty for metric " + metricCollectionInfo.getMetricName());
        return;
      }

      if (metricCollectionInfo.getResponseMapping() == null) {
        invalidFields.put("responseMapping",
            "Valid JSON Mappings for the response have not been provided for " + metricCollectionInfo.getMetricName());
        return;
      }

      ResponseMapping mapping = metricCollectionInfo.getResponseMapping();

      if (isEmpty(mapping.getMetricValueJsonPath())) {
        invalidFields.put("metricValueJsonPath/timestampJsonPath",
            "Metric value path is empty for " + metricCollectionInfo.getMetricName());
        return;
      }

      if (isEmpty(mapping.getTxnNameFieldValue()) && isEmpty(mapping.getTxnNameJsonPath())) {
        invalidFields.put("transactionName", "Transaction Name is empty for " + metricCollectionInfo.getMetricName());
        return;
      }

      if (metricCollectionInfo.getCollectionUrl().contains(VERIFICATION_HOST_PLACEHOLDER)
          && isNotEmpty(metricCollectionInfo.getBaselineCollectionUrl())) {
        invalidFields.put("collectionUrl",
            "for " + metricCollectionInfo.getMetricName() + " the collection url has " + VERIFICATION_HOST_PLACEHOLDER
                + " and baseline collection url as well");
        return;
      }

      if (hasBaselineUrl.get() && metricCollectionInfo.getCollectionUrl().contains(VERIFICATION_HOST_PLACEHOLDER)) {
        invalidFields.put("collectionUrl",
            "for " + metricCollectionInfo.getMetricName() + " the url has " + VERIFICATION_HOST_PLACEHOLDER
                + ". When configuring multi url verification all metrics should follow the same pattern.");
        return;
      }

      if (isNotEmpty(metricCollectionInfo.getBaselineCollectionUrl())) {
        hasBaselineUrl.set(true);
        if (!AnalysisComparisonStrategy.COMPARE_WITH_CURRENT.equals(getComparisonStrategy())) {
          invalidFields.put("collectionUrl",
              "Baseline url can only be set for canary verification strategy. For "
                  + metricCollectionInfo.getMetricName() + " there is baseline url set "
                  + VERIFICATION_HOST_PLACEHOLDER);
          return;
        }

        if (metricCollectionInfo.getBaselineCollectionUrl().contains(VERIFICATION_HOST_PLACEHOLDER)) {
          invalidFields.put("collectionUrl",
              "for " + metricCollectionInfo.getMetricName() + " baseline url contains "
                  + VERIFICATION_HOST_PLACEHOLDER);
          return;
        }
      }
    });

    return invalidFields;
  }

  private Map<String, TimeSeriesMetricDefinition> metricDefinitions(Collection<List<APMMetricInfo>> metricInfos) {
    Map<String, TimeSeriesMetricDefinition> metricTypeMap = new HashMap<>();
    metricInfos.forEach(metricInfoList
        -> metricInfoList.forEach(metricInfo
            -> metricTypeMap.put(metricInfo.getMetricName(),
                TimeSeriesMetricDefinition.builder()
                    .metricName(metricInfo.getMetricName())
                    .metricType(metricInfo.getMetricType())
                    .tags(Sets.newHashSet(metricInfo.getTag()))
                    .build())));
    return metricTypeMap;
  }

  public static Map<String, TimeSeriesMetricGroup.TimeSeriesMlAnalysisGroupInfo> metricGroup(
      Map<String, List<APMMetricInfo>> metricInfos) {
    Set<String> groups = new HashSet<>();
    for (List<APMMetricInfo> metricInfoList : metricInfos.values()) {
      for (APMMetricInfo metricInfo : metricInfoList) {
        groups.add(metricInfo.getTag());
      }
    }
    Map<String, TimeSeriesMetricGroup.TimeSeriesMlAnalysisGroupInfo> groupInfoMap = new HashMap<>();
    for (String group : groups) {
      groupInfoMap.put(group,
          TimeSeriesMetricGroup.TimeSeriesMlAnalysisGroupInfo.builder()
              .groupName(group)
              .mlAnalysisType(TimeSeriesMlAnalysisType.COMPARATIVE)
              .build());
    }
    if (groupInfoMap.size() == 0) {
      throw new WingsException("No Metric Group Names found. This is a required field");
    }
    return groupInfoMap;
  }

  @Override
  protected String triggerAnalysisDataCollection(ExecutionContext context, AnalysisContext analysisContext,
      VerificationStateAnalysisExecutionData executionData, Map<String, String> hosts) {
    WorkflowStandardParams workflowStandardParams = context.getContextElement(ContextElementType.STANDARD);

    String envId = workflowStandardParams == null ? null : workflowStandardParams.getEnv().getUuid();

    SettingAttribute settingAttribute = null;
    String serverConfigId = analysisServerConfigId;
    if (!isEmpty(getTemplateExpressions())) {
      TemplateExpression configIdExpression =
          templateExpressionProcessor.getTemplateExpression(getTemplateExpressions(), "analysisServerConfigId");
      if (configIdExpression != null) {
        settingAttribute = templateExpressionProcessor.resolveSettingAttribute(context, configIdExpression);
        serverConfigId = settingAttribute.getUuid();
      }
    }
    if (settingAttribute == null) {
      settingAttribute = settingsService.get(serverConfigId);
      if (settingAttribute == null) {
        throw new WingsException("No Datadog setting with id: " + analysisServerConfigId + " found");
      }
    }

    final APMVerificationConfig apmConfig = (APMVerificationConfig) settingAttribute.getValue();
    List<APMMetricInfo> canaryMetricInfos = getCanaryMetricInfos(context);
    Map<String, List<APMMetricInfo>> apmMetricInfos =
        isNotEmpty(canaryMetricInfos) ? new HashMap<>() : apmMetricInfos(context);
    metricAnalysisService.saveMetricTemplates(context.getAppId(), StateType.APM_VERIFICATION,
        context.getStateExecutionInstanceId(), null,
        metricDefinitions(
            isNotEmpty(canaryMetricInfos) ? Collections.singletonList(canaryMetricInfos) : apmMetricInfos.values()));
    final long dataCollectionStartTimeStamp = dataCollectionStartTimestampMillis();
    String accountId = appService.get(context.getAppId()).getAccountId();
    final APMDataCollectionInfo dataCollectionInfo =
        APMDataCollectionInfo.builder()
            .baseUrl(apmConfig.getUrl())
            .validationUrl(apmConfig.getValidationUrl())
            .headers(apmConfig.collectionHeaders())
            .options(apmConfig.collectionParams())
            .encryptedDataDetails(apmConfig.encryptedDataDetails(secretManager))
            .hosts(hosts)
            .stateType(StateType.APM_VERIFICATION)
            .applicationId(context.getAppId())
            .stateExecutionId(context.getStateExecutionInstanceId())
            .workflowId(getWorkflowId(context))
            .workflowExecutionId(context.getWorkflowExecutionId())
            .serviceId(getPhaseServiceId(context))
            .startTime(dataCollectionStartTimeStamp)
            .dataCollectionMinute(0)
            .dataCollectionFrequency(getDataCollectionRate())
            .dataCollectionTotalTime(Integer.parseInt(getTimeDuration()))
            .metricEndpoints(apmMetricInfos)
            .canaryMetricInfos(canaryMetricInfos)
            .accountId(accountId)
            .strategy(getComparisonStrategy())
            .build();

    analysisContext.getTestNodes().put(TEST_HOST_NAME, DEFAULT_GROUP_NAME);
    if (getComparisonStrategy() == AnalysisComparisonStrategy.COMPARE_WITH_CURRENT) {
      analysisContext.getControlNodes().put(CONTROL_HOST_NAME, DEFAULT_GROUP_NAME);
      for (int i = 1; i <= CANARY_DAYS_TO_COLLECT; ++i) {
        analysisContext.getControlNodes().put(CONTROL_HOST_NAME + "-" + i, DEFAULT_GROUP_NAME);
      }
    }

    String waitId = generateUuid();
    String infrastructureMappingId = context.fetchInfraMappingId();
    DelegateTask delegateTask =
        DelegateTask.builder()
            .async(true)
            .accountId(accountId)
            .appId(context.getAppId())
            .waitId(waitId)
            .data(TaskData.builder()
                      .taskType(TaskType.APM_METRIC_DATA_COLLECTION_TASK.name())
                      .parameters(new Object[] {dataCollectionInfo})
                      .timeout(TimeUnit.MINUTES.toMillis(Integer.parseInt(getTimeDuration()) + 120))
                      .build())
            .envId(envId)
            .infrastructureMappingId(infrastructureMappingId)
            .build();
    waitNotifyEngine.waitForAll(DataCollectionCallback.builder()
                                    .appId(context.getAppId())
                                    .stateExecutionId(context.getStateExecutionInstanceId())
                                    .executionData(executionData)
                                    .build(),
        waitId);
    return delegateService.queueTask(delegateTask);
  }

  public Map<String, List<APMMetricInfo>> apmMetricInfos(final ExecutionContext context) {
    Map<String, List<APMMetricInfo>> metricInfoMap = new HashMap<>();
    for (MetricCollectionInfo metricCollectionInfo : metricCollectionInfos) {
      String evaluatedUrl = context.renderExpression(metricCollectionInfo.getCollectionUrl());

      if (metricCollectionInfo.getMethod() != null && metricCollectionInfo.getMethod().equals(Method.POST)) {
        evaluatedUrl += URL_BODY_APPENDER + metricCollectionInfo.getCollectionBody();
      }

      if (!metricInfoMap.containsKey(evaluatedUrl)) {
        metricInfoMap.put(evaluatedUrl, new ArrayList<>());
      }
      APMMetricInfo metricInfo = APMMetricInfo.builder()
                                     .metricName(metricCollectionInfo.getMetricName())
                                     .metricType(metricCollectionInfo.getMetricType())
                                     .method(metricCollectionInfo.getMethod())
                                     .body(context.renderExpression(metricCollectionInfo.getCollectionBody()))
                                     .tag(metricCollectionInfo.getTag())
                                     .responseMappers(getResponseMappers(metricCollectionInfo))
                                     .build();
      logger.info("In APMMetricInfos, evaluatedUrl is: {}", evaluatedUrl);
      metricInfoMap.get(evaluatedUrl).add(metricInfo);
    }
    return metricInfoMap;
  }

  private List<APMMetricInfo> getCanaryMetricInfos(final ExecutionContext context) {
    if (!AnalysisComparisonStrategy.COMPARE_WITH_CURRENT.equals(getComparisonStrategy())) {
      return Collections.emptyList();
    }

    List<APMMetricInfo> metricInfos = new ArrayList<>();
    for (MetricCollectionInfo metricCollectionInfo : metricCollectionInfos) {
      if (isEmpty(metricCollectionInfo.getBaselineCollectionUrl())) {
        continue;
      }
      String testUrl = context.renderExpression(metricCollectionInfo.getCollectionUrl());
      String controlUrl = context.renderExpression(metricCollectionInfo.getBaselineCollectionUrl());

      if (metricCollectionInfo.getMethod() != null && metricCollectionInfo.getMethod().equals(Method.POST)) {
        testUrl += URL_BODY_APPENDER + metricCollectionInfo.getCollectionBody();
        controlUrl += URL_BODY_APPENDER + metricCollectionInfo.getCollectionBody();
      }

      metricInfos.add(APMMetricInfo.builder()
                          .url(testUrl)
                          .hostName(TEST_HOST_NAME)
                          .metricName(metricCollectionInfo.getMetricName())
                          .metricType(metricCollectionInfo.getMetricType())
                          .method(metricCollectionInfo.getMethod())
                          .body(context.renderExpression(metricCollectionInfo.getCollectionBody()))
                          .tag(metricCollectionInfo.getTag())
                          .responseMappers(getResponseMappers(metricCollectionInfo))
                          .build());

      metricInfos.add(APMMetricInfo.builder()
                          .url(controlUrl)
                          .hostName(CONTROL_HOST_NAME)
                          .metricName(metricCollectionInfo.getMetricName())
                          .metricType(metricCollectionInfo.getMetricType())
                          .method(metricCollectionInfo.getMethod())
                          .body(context.renderExpression(metricCollectionInfo.getCollectionBody()))
                          .tag(metricCollectionInfo.getTag())
                          .responseMappers(getResponseMappers(metricCollectionInfo))
                          .build());
    }
    logger.info("for {} canaryInfo is {}", context.getStateExecutionInstanceId(), metricInfos);
    return metricInfos;
  }

  private Map<String, ResponseMapper> getResponseMappers(MetricCollectionInfo metricCollectionInfo) {
    ResponseMapping responseMapping = metricCollectionInfo.getResponseMapping();
    Map<String, ResponseMapper> responseMappers = new HashMap<>();
    List<String> txnRegex =
        responseMapping.getTxnNameRegex() == null ? null : Lists.newArrayList(responseMapping.getTxnNameRegex());
    ResponseMapper txnNameResponseMapper = ResponseMapper.builder().fieldName("txnName").regexs(txnRegex).build();
    if (!isEmpty(responseMapping.getTxnNameFieldValue())) {
      txnNameResponseMapper.setFieldValue(responseMapping.getTxnNameFieldValue());
    } else {
      txnNameResponseMapper.setJsonPath(responseMapping.getTxnNameJsonPath());
    }
    // Set the host details (if exists) in the responseMapper
    if (!isEmpty(responseMapping.getHostJsonPath())) {
      String hostJson = responseMapping.getHostJsonPath();
      List<String> hostRegex =
          isEmpty(responseMapping.getHostRegex()) ? null : Lists.newArrayList(responseMapping.getHostRegex());
      ResponseMapper hostResponseMapper =
          ResponseMapper.builder().fieldName("host").regexs(hostRegex).jsonPath(hostJson).build();
      responseMappers.put("host", hostResponseMapper);
    }
    responseMappers.put("txnName", txnNameResponseMapper);
    if (isNotEmpty(responseMapping.getTimestampJsonPath())
        && isNotEmpty(responseMapping.getTimestampJsonPath().trim())) {
      responseMappers.put("timestamp",
          ResponseMapper.builder()
              .fieldName("timestamp")
              .jsonPath(responseMapping.getTimestampJsonPath())
              .timestampFormat(responseMapping.getTimestampFormat())
              .build());
    }

    responseMappers.put("value",
        ResponseMapper.builder().fieldName("value").jsonPath(responseMapping.getMetricValueJsonPath()).build());

    return responseMappers;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetricCollectionInfo {
    private String metricName;
    private MetricType metricType;
    private String tag;
    private String collectionUrl;
    private String baselineCollectionUrl;
    private String collectionBody;
    private ResponseType responseType;
    private ResponseMapping responseMapping;
    private Method method;

    public String getCollectionUrl() {
      try {
        return collectionUrl.replaceAll("`", URLEncoder.encode("`", "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new VerificationOperationException(ErrorCode.APM_CONFIGURATION_ERROR,
            "Unsupported encoding exception while encoding backticks in " + collectionUrl);
      }
    }

    public String getBaselineCollectionUrl() {
      if (isEmpty(baselineCollectionUrl)) {
        return null;
      }
      try {
        return baselineCollectionUrl.replaceAll("`", URLEncoder.encode("`", "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new VerificationOperationException(ErrorCode.APM_CONFIGURATION_ERROR,
            "Unsupported encoding exception while encoding backticks in " + baselineCollectionUrl);
      }
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ResponseMapping {
    private String txnNameFieldValue;
    private String txnNameJsonPath;
    private String txnNameRegex;
    private String metricValueJsonPath;
    private String hostJsonPath;
    private String hostRegex;
    private String timestampJsonPath;
    private String timestampFormat;
  }

  public enum ResponseType { JSON }

  public enum Method { POST, GET }
}
