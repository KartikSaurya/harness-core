/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans.ce;

import static software.wings.audit.ResourceType.CE_CONNECTOR;

import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.expression.ExpressionEvaluator;

import software.wings.settings.SettingValue;
import software.wings.settings.SettingVariableTypes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.reinert.jjschema.Attributes;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.NotEmpty;

@JsonTypeName("CE_AZURE")
@Data
@Builder
@FieldNameConstants(innerTypeName = "CEAzureConfigKeys")
@EqualsAndHashCode(callSuper = false)
public class CEAzureConfig extends SettingValue {
  @Attributes(required = true) @NotEmpty String directoryName;
  @Attributes(required = true) @NotEmpty String containerName;
  @Attributes(required = true) @NotEmpty String storageAccountName;
  @Attributes(required = true) @NotEmpty String subscriptionId;
  @Attributes(required = true) @NotEmpty String tenantId;

  @Override
  public String fetchResourceCategory() {
    return CE_CONNECTOR.name();
  }

  @Override
  public List<ExecutionCapability> fetchRequiredExecutionCapabilities(ExpressionEvaluator maskingEvaluator) {
    return null;
  }

  public CEAzureConfig() {
    super(SettingVariableTypes.CE_AZURE.name());
  }

  public CEAzureConfig(
      String directoryName, String containerName, String storageAccountName, String subscriptionId, String tenantId) {
    this();
    this.directoryName = directoryName;
    this.containerName = containerName;
    this.storageAccountName = storageAccountName;
    this.subscriptionId = subscriptionId;
    this.tenantId = tenantId;
  }
}
