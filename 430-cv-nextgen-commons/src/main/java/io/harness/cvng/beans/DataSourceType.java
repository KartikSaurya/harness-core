/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cvng.beans;

import io.harness.cvng.models.VerificationType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum DataSourceType {
  APP_DYNAMICS("Appdynamics", VerificationType.TIME_SERIES, "appdynamics"),
  SPLUNK("Splunk", VerificationType.LOG, "splunk"),
  STACKDRIVER("Stackdriver", VerificationType.TIME_SERIES, "prometheus"),
  STACKDRIVER_LOG("Stackdriver Log", VerificationType.LOG, "splunk"),
  KUBERNETES("Kubernetes", VerificationType.TIME_SERIES, "prometheus"),
  NEW_RELIC("New Relic", VerificationType.TIME_SERIES, "appdynamics"),
  PROMETHEUS("Prometheus", VerificationType.TIME_SERIES, "prometheus");

  private String displayName;
  private VerificationType verificationType;
  // template prefix that should be used for demo data.
  private String demoTemplatePrefix;

  DataSourceType(String displayName, VerificationType verificationType, String demoTemplatePrefix) {
    this.displayName = displayName;
    this.verificationType = verificationType;
    this.demoTemplatePrefix = demoTemplatePrefix;
  }

  public String getDisplayName() {
    return displayName;
  }

  public VerificationType getVerificationType() {
    return verificationType;
  }

  public static List<DataSourceType> getTimeSeriesTypes() {
    return new ArrayList<>(EnumSet.of(APP_DYNAMICS, STACKDRIVER, NEW_RELIC, PROMETHEUS));
  }

  public String getDemoTemplatePrefix() {
    return demoTemplatePrefix;
  }
}
