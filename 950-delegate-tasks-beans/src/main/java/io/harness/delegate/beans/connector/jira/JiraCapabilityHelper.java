/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.beans.connector.jira;

import io.harness.delegate.beans.connector.ConnectorCapabilityBaseHelper;
import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.delegate.task.mixin.HttpConnectionExecutionCapabilityGenerator;
import io.harness.expression.ExpressionEvaluator;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JiraCapabilityHelper extends ConnectorCapabilityBaseHelper {
  public List<ExecutionCapability> fetchRequiredExecutionCapabilities(
      ExpressionEvaluator maskingEvaluator, JiraConnectorDTO jiraConnectorDTO) {
    List<ExecutionCapability> capabilityList = new ArrayList<>();
    String jiraUrl = jiraConnectorDTO.getJiraUrl();
    capabilityList.add(HttpConnectionExecutionCapabilityGenerator.buildHttpConnectionExecutionCapability(
        jiraUrl.endsWith("/") ? jiraUrl : jiraUrl.concat("/"), maskingEvaluator));
    populateDelegateSelectorCapability(capabilityList, jiraConnectorDTO.getDelegateSelectors());
    return capabilityList;
  }
}
