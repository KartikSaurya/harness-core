package software.wings.beans;

import lombok.Getter;
import software.wings.beans.FeatureFlag.Scope;

/**
 * Add your feature name here. When the feature is fully launched and no longer needs to be flagged,
 * delete the feature name.
 */
public enum FeatureName {
  CV_DEMO,
  DISABLE_LOGML_NEURAL_NET,
  GIT_BATCH_SYNC,
  GLOBAL_CV_DASH,
  CV_SUCCEED_FOR_ANOMALY,
  COPY_ARTIFACT,
  INLINE_SSH_COMMAND,
  CV_DATA_COLLECTION_JOB,
  THREE_PHASE_SECRET_DECRYPTION,
  DELEGATE_CAPABILITY_FRAMEWORK,
  GRAPHQL,
  DISABLE_METRIC_NAME_CURLY_BRACE_CHECK,
  GLOBAL_DISABLE_HEALTH_CHECK(Scope.GLOBAL),
  GIT_HTTPS_KERBEROS,
  TRIGGER_FOR_ALL_ARTIFACTS,
  USE_PCF_CLI,
  AUDIT_TRAIL_UI,
  ARTIFACT_STREAM_REFACTOR,
  TRIGGER_REFACTOR,
  TRIGGER_YAML,
  CV_FEEDBACKS,
  CV_HOST_SAMPLING,
  CUSTOM_DASHBOARD,
  SEND_LOG_ANALYSIS_COMPRESSED,
  INFRA_MAPPING_REFACTOR,
  GRAPHQL_DEV,
  SUPERVISED_TS_THRESHOLD,
  REJECT_TRIGGER_IF_ARTIFACTS_NOT_MATCH,
  NEW_INSTANCE_TIMESERIES,
  SPOTINST,
  ENTITY_AUDIT_RECORD,
  TIME_RANGE_FREEZE_GOVERNANCE,
  SCIM_INTEGRATION,
  NEW_RELIC_CV_TASK,
  SLACK_APPROVALS,
  NEWRELIC_24_7_CV_TASK,
  SEARCH(Scope.GLOBAL),
  PIPELINE_GOVERNANCE,
  TEMPLATED_PIPELINES,
  SEARCH_REQUEST,
  ON_DEMAND_ROLLBACK,
  NODE_AGGREGATION,
  BIND_FETCH_FILES_TASK_TO_DELEGATE,
  DEFAULT_ARTIFACT,
  DEPLOY_TO_SPECIFIC_HOSTS,
  UI_ALLOW_K8S_V1,
  SEND_SLACK_NOTIFICATION_FROM_DELEGATE,
  LOGS_V2_247,
  WEEKLY_WINDOW,
  SSH_WINRM_SO,
  APM_CUSTOM_THRESHOLDS,
  SALESFORCE_INTEGRATION,
  SWITCH_GLOBAL_TO_GCP_KMS,
  FAIL_FAST_THRESHOLDS_WORKFLOW,
  FAIL_FAST_THRESHOLDS_SERVICEGUARD,
  SIDE_NAVIGATION,
  WORKFLOW_TS_RECORDS_NEW,
  TEMPLATE_YAML_SUPPORT,
  ADD_WORKFLOW_FORMIK,
  CV_INSTANA,
  CV_INSTANA_24X7,
  CUSTOM_APM_CV_TASK,
  CUSTOM_APM_24_X_7_CV_TASK,
  SERVICE_GUARD_ACTIVITY_LOGS,
  VANITY_URL,
  GIT_SYNC_REFACTOR,
  DISABLE_SERVICEGUARD_LOG_ALERTS,
  ARTIFACT_PERPETUAL_TASK,
  DEPLOYMENT_TAGS;

  FeatureName() {
    scope = Scope.PER_ACCOUNT;
  }

  FeatureName(Scope scope) {
    this.scope = scope;
  }

  @Getter private FeatureFlag.Scope scope;
}
