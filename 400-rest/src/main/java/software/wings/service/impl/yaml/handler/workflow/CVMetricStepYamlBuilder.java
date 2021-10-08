package software.wings.service.impl.yaml.handler.workflow;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.data.structure.UUIDGenerator.generateUuid;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
import io.harness.serializer.JsonUtils;

import software.wings.service.impl.analysis.TimeSeriesMLTransactionThresholds;
import software.wings.service.impl.analysis.TimeSeriesMLTransactionThresholds.TimeSeriesMLTransactionThresholdsDTO;
import software.wings.service.intfc.MetricDataAnalysisService;
import software.wings.sm.StateType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.CV)
@TargetModule(HarnessModule._870_CG_ORCHESTRATION)
@Slf4j
public abstract class CVMetricStepYamlBuilder extends StepYamlBuilder {
  private static final String CUSTOM_THRESHOLD_KEY = "customThresholdRefId";
  private static final String THRESHOLDS_KEY = "metric-thresholds";
  @Inject private MetricDataAnalysisService metricDataAnalysisService;

  @Override
  public void convertIdToNameForKnownTypes(String name, Object objectValue, Map<String, Object> outputProperties,
      String appId, Map<String, Object> inputProperties) {
    if (name.equals(CUSTOM_THRESHOLD_KEY)) {
      List<TimeSeriesMLTransactionThresholds> thresholds =
          metricDataAnalysisService.getCustomThreshold((String) objectValue);
      List<TimeSeriesMLTransactionThresholdsDTO> thresholdsDTOList = new ArrayList<>();
      if (isNotEmpty(thresholds)) {
        thresholds.forEach(threshold
            -> thresholdsDTOList.add(TimeSeriesMLTransactionThresholdsDTO.fromTransactionThresholdsEntity(threshold)));
      }
      outputProperties.put(THRESHOLDS_KEY, thresholdsDTOList);
    }
    if (!name.equals(THRESHOLDS_KEY)) {
      outputProperties.put(name, objectValue);
    }
  }

  @Override
  public void convertNameToIdForKnownTypes(String name, Object objectValue, Map<String, Object> outputProperties,
      String appId, String accountId, Map<String, Object> inputProperties) {
    if (!name.equals(THRESHOLDS_KEY)) {
      super.convertNameToIdForKnownTypes(name, objectValue, outputProperties, appId, accountId, inputProperties);
    } else {
      if (inputProperties.get(CUSTOM_THRESHOLD_KEY) == null) {
        // no threshold refIds yet. So create one
        String cutomThresholdId = generateUuid();
        outputProperties.put(CUSTOM_THRESHOLD_KEY, cutomThresholdId);
        final Gson gson = new Gson();
        Type type = new TypeToken<List<TimeSeriesMLTransactionThresholdsDTO>>() {}.getType();
        List<TimeSeriesMLTransactionThresholdsDTO> thresholdsDTOList =
            gson.fromJson(JsonUtils.asJson(objectValue), type);
        if (thresholdsDTOList != null) {
          List<TimeSeriesMLTransactionThresholds> thresholds = new ArrayList<>();
          thresholdsDTOList.forEach(dto -> thresholds.add(dto.toEntity(cutomThresholdId)));
          metricDataAnalysisService.saveCustomThreshold(null, null, thresholds);
        }
      } else {
        String customThresholdRefId = (String) inputProperties.get(CUSTOM_THRESHOLD_KEY);
        List<TimeSeriesMLTransactionThresholds> thresholdsInDB =
            metricDataAnalysisService.getCustomThreshold(customThresholdRefId);
        Set<String> thresholdIds =
            thresholdsInDB.stream().map(TimeSeriesMLTransactionThresholds::getUuid).collect(Collectors.toSet());
        final Gson gson = new Gson();
        Type type = new TypeToken<List<TimeSeriesMLTransactionThresholdsDTO>>() {}.getType();
        List<TimeSeriesMLTransactionThresholdsDTO> thresholdsDTOList =
            gson.fromJson(JsonUtils.asJson(objectValue), type);
        if (thresholdsDTOList != null) {
          List<TimeSeriesMLTransactionThresholds> thresholds = new ArrayList<>();
          thresholdsDTOList.forEach(dto -> thresholds.add(dto.toEntity(customThresholdRefId)));
          metricDataAnalysisService.saveCustomThreshold(null, null, thresholds);
          metricDataAnalysisService.deleteCustomThreshold(new ArrayList<>(thresholdIds));
        }
      }
    }
  }

  public abstract StateType getStateType();
}
