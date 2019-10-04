package software.wings.service.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static software.wings.beans.EntityType.APPDYNAMICS_APPID;
import static software.wings.beans.EntityType.APPDYNAMICS_CONFIGID;
import static software.wings.beans.EntityType.APPDYNAMICS_TIERID;
import static software.wings.beans.EntityType.ELK_CONFIGID;
import static software.wings.beans.EntityType.ELK_INDICES;
import static software.wings.beans.EntityType.ENVIRONMENT;
import static software.wings.beans.EntityType.INFRASTRUCTURE_DEFINITION;
import static software.wings.beans.EntityType.INFRASTRUCTURE_MAPPING;
import static software.wings.beans.EntityType.NEWRELIC_APPID;
import static software.wings.beans.EntityType.NEWRELIC_CONFIGID;
import static software.wings.beans.EntityType.NEWRELIC_MARKER_APPID;
import static software.wings.beans.EntityType.NEWRELIC_MARKER_CONFIGID;
import static software.wings.beans.EntityType.SERVICE;
import static software.wings.beans.Variable.VariableBuilder.aVariable;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import io.harness.category.element.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import software.wings.WingsBaseTest;
import software.wings.beans.Variable;
import software.wings.beans.VariableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineServiceImplTest extends WingsBaseTest {
  @Inject @InjectMocks private PipelineServiceImpl pipelineServiceImpl;

  @Test
  @Category(UnitTests.class)
  public void testPopulateParentFields() {
    List<Variable> workflowVariables = asList(
        aVariable()
            .entityType(SERVICE)
            .name("Service")
            .value("Service 1")
            .metadata(ImmutableMap.of(Variable.RELATED_FIELD, "ServiceInfra_ECS", Variable.ENTITY_TYPE, SERVICE))
            .build(),
        aVariable()
            .entityType(ENVIRONMENT)
            .name("Environment")
            .value("env 1")
            .metadata(ImmutableMap.of(Variable.RELATED_FIELD, "ServiceInfra_ECS", Variable.ENTITY_TYPE, ENVIRONMENT))
            .build(),
        aVariable().entityType(INFRASTRUCTURE_MAPPING).name("ServiceInfra_ECS").value("${Infra}").build(),
        aVariable()
            .entityType(APPDYNAMICS_APPID)
            .name("AppdAppId")
            .value("AppD app")
            .metadata(ImmutableMap.of(Variable.RELATED_FIELD, "AppdTierId", Variable.ENTITY_TYPE, APPDYNAMICS_APPID))
            .build(),
        aVariable()
            .entityType(APPDYNAMICS_CONFIGID)
            .name("AppdConfigId")
            .value("AppD config")
            .metadata(ImmutableMap.of(Variable.RELATED_FIELD, "AppdTierId", Variable.ENTITY_TYPE, APPDYNAMICS_CONFIGID))
            .build(),
        aVariable()
            .entityType(ELK_CONFIGID)
            .name("ElkConfigId")
            .value("elk config")
            .metadata(ImmutableMap.of(Variable.RELATED_FIELD, "ElkIndices", Variable.ENTITY_TYPE, ELK_CONFIGID))
            .build(),
        aVariable().entityType(ELK_INDICES).name("ElkIndices").value("elk indices").build(),
        aVariable().entityType(NEWRELIC_APPID).name("NewRelicAppId").value("NewRelic appId").build(),
        aVariable()
            .entityType(NEWRELIC_CONFIGID)
            .name("NewRelicConfigId")
            .value("NewRelic configId")
            .metadata(ImmutableMap.of(Variable.RELATED_FIELD, "NewRelicAppId", Variable.ENTITY_TYPE, NEWRELIC_CONFIGID))
            .build(),
        aVariable()
            .entityType(NEWRELIC_MARKER_APPID)
            .name("NewRelicMarkerAppId")
            .value("NewRelic Marker appId")
            .build(),
        aVariable()
            .entityType(NEWRELIC_MARKER_CONFIGID)
            .name("NewRelicMarkerConfigId")
            .value("NewRelic Marker configId")
            .metadata(ImmutableMap.of(
                Variable.RELATED_FIELD, "NewRelicMarkerAppId", Variable.ENTITY_TYPE, NEWRELIC_MARKER_CONFIGID))
            .build(),
        aVariable().entityType(APPDYNAMICS_TIERID).name("AppdTierId").value("${Tier}").build(),
        aVariable().type(VariableType.TEXT).name("test").value("test").build());

    Map<String, String> pseWorkflowVariables = new HashMap<>();
    pseWorkflowVariables.put("Service", "Service 2");
    pseWorkflowVariables.put("Environment", "Environment 2");
    pseWorkflowVariables.put("ServiceInfra_ECS", "{$infra}");
    pseWorkflowVariables.put("AppdAppId", "AppD app 1");
    pseWorkflowVariables.put("AppdTierId", "AppD tier");
    pseWorkflowVariables.put("AppdConfigId", "AppD config 2");
    pseWorkflowVariables.put("NewRelicAppId", "NewRelicAppId2");
    pseWorkflowVariables.put("NewRelicMarkerAppId", "${app}");
    pseWorkflowVariables.put("ElkIndices", "elk");
    pseWorkflowVariables.put("ElkConfigId", "elkconfig");
    pseWorkflowVariables.put("NewRelicMarkerConfigId", "newRelicMarkerconfigId");
    pseWorkflowVariables.put("NewRelicConfigId", "newRelicConfigId");

    // Infra mapping variable populate test
    Map<String, Object> metadataMapInfra = new HashMap<>();
    metadataMapInfra.put("entityType", INFRASTRUCTURE_MAPPING);
    Variable infraMappingPipelineVar = aVariable()
                                           .name("Infra")
                                           .description("Variable for Service Infra-structure entity")
                                           .type(VariableType.ENTITY)
                                           .mandatory(true)
                                           .fixed(false)
                                           .metadata(metadataMapInfra)
                                           .build();
    pipelineServiceImpl.populateParentFields(
        infraMappingPipelineVar, INFRASTRUCTURE_MAPPING, workflowVariables, "ServiceInfra_ECS", pseWorkflowVariables);
    assertThat(infraMappingPipelineVar.getMetadata().get(Variable.ENV_ID)).isNotNull();
    assertThat(infraMappingPipelineVar.getMetadata().get(Variable.ENV_ID)).isEqualTo("Environment 2");

    assertThat(infraMappingPipelineVar.getMetadata().get(Variable.SERVICE_ID)).isNotNull();
    assertThat(infraMappingPipelineVar.getMetadata().get(Variable.SERVICE_ID)).isEqualTo("Service 2");

    // Infra mapping definition populate test
    Map<String, Object> metadataMapInfraDef = new HashMap<>();
    metadataMapInfra.put("entityType", INFRASTRUCTURE_DEFINITION);
    Variable infraDefPipelineVar = aVariable()
                                       .name("Infra")
                                       .description("Variable for Service Infra-structure entity")
                                       .type(VariableType.ENTITY)
                                       .mandatory(true)
                                       .fixed(false)
                                       .metadata(metadataMapInfraDef)
                                       .build();

    pipelineServiceImpl.populateParentFields(
        infraDefPipelineVar, INFRASTRUCTURE_DEFINITION, workflowVariables, "ServiceInfra_ECS", pseWorkflowVariables);
    assertThat(infraDefPipelineVar.getMetadata().get(Variable.ENV_ID)).isNotNull();
    assertThat(infraDefPipelineVar.getMetadata().get(Variable.ENV_ID)).isEqualTo("Environment 2");

    assertThat(infraDefPipelineVar.getMetadata().get(Variable.SERVICE_ID)).isNotNull();
    assertThat(infraDefPipelineVar.getMetadata().get(Variable.SERVICE_ID)).isEqualTo("Service 2");

    // Appdynamics tierId populate test

    Map<String, Object> metadataMapAppdTier = new HashMap<>();
    metadataMapAppdTier.put("entityType", APPDYNAMICS_TIERID);
    Variable appdTierPipelineVar = aVariable()
                                       .name("Tier")
                                       .description("Variable for Appd Tier")
                                       .type(VariableType.ENTITY)
                                       .mandatory(true)
                                       .fixed(false)
                                       .metadata(metadataMapAppdTier)
                                       .build();

    pipelineServiceImpl.populateParentFields(
        appdTierPipelineVar, APPDYNAMICS_TIERID, workflowVariables, "AppdTierId", pseWorkflowVariables);
    assertThat(appdTierPipelineVar.getMetadata().get(Variable.PARENT_FIELDS)).isNotNull();
    Map<String, String> parents = (Map<String, String>) appdTierPipelineVar.getMetadata().get(Variable.PARENT_FIELDS);
    assertThat(parents.get("applicationId")).isEqualTo("AppD app 1");
    assertThat(parents.get("analysisServerConfigId")).isEqualTo("AppD config 2");

    // Appdynamics appId populate test
    Map<String, Object> metadataMapAppdApp = new HashMap<>();
    metadataMapAppdApp.put("entityType", APPDYNAMICS_APPID);
    Variable appdAppPipelineVar = aVariable()
                                      .name("App")
                                      .description("Variable for Appd App")
                                      .type(VariableType.ENTITY)
                                      .mandatory(true)
                                      .fixed(false)
                                      .metadata(metadataMapAppdApp)
                                      .build();

    pipelineServiceImpl.populateParentFields(
        appdAppPipelineVar, APPDYNAMICS_APPID, workflowVariables, "AppdTierId", pseWorkflowVariables);
    assertThat(appdAppPipelineVar.getMetadata().get(Variable.PARENT_FIELDS)).isNotNull();
    parents = (Map<String, String>) appdAppPipelineVar.getMetadata().get(Variable.PARENT_FIELDS);
    assertThat(parents.get("analysisServerConfigId")).isEqualTo("AppD config 2");

    // Elk indices populate test
    Map<String, Object> metadataMapElkIndices = new HashMap<>();
    metadataMapElkIndices.put("entityType", ELK_INDICES);
    Variable elkIndicesPipelineVar = aVariable()
                                         .name("Elk")
                                         .description("Variable for elk type")
                                         .type(VariableType.ENTITY)
                                         .mandatory(true)
                                         .fixed(false)
                                         .metadata(metadataMapElkIndices)
                                         .build();

    pipelineServiceImpl.populateParentFields(
        elkIndicesPipelineVar, ELK_INDICES, workflowVariables, "ElkIndices", pseWorkflowVariables);
    assertThat(elkIndicesPipelineVar.getMetadata().get(Variable.PARENT_FIELDS)).isNotNull();
    parents = (Map<String, String>) elkIndicesPipelineVar.getMetadata().get(Variable.PARENT_FIELDS);
    assertThat(parents.get("analysisServerConfigId")).isEqualTo("elkconfig");

    // New relic populate test
    Map<String, Object> metadataMapNewRelicIndices = new HashMap<>();
    metadataMapNewRelicIndices.put("entityType", NEWRELIC_APPID);
    Variable newRelicPipelineVar = aVariable()
                                       .name("newRelic")
                                       .description("Variable for newrelic type")
                                       .type(VariableType.ENTITY)
                                       .mandatory(true)
                                       .fixed(false)
                                       .metadata(metadataMapNewRelicIndices)
                                       .build();

    pipelineServiceImpl.populateParentFields(
        newRelicPipelineVar, NEWRELIC_APPID, workflowVariables, "NewRelicAppId", pseWorkflowVariables);
    assertThat(newRelicPipelineVar.getMetadata().get(Variable.PARENT_FIELDS)).isNotNull();
    parents = (Map<String, String>) newRelicPipelineVar.getMetadata().get(Variable.PARENT_FIELDS);
    assertThat(parents.get("analysisServerConfigId")).isEqualTo("newRelicConfigId");

    // New relic marker populate test
    Map<String, Object> metadataMapNewRelicMarkerIndices = new HashMap<>();
    metadataMapNewRelicMarkerIndices.put("entityType", NEWRELIC_MARKER_APPID);
    Variable newRelicMarkerPipelineVar = aVariable()
                                             .name("newRelic")
                                             .description("Variable for newrelic type")
                                             .type(VariableType.ENTITY)
                                             .mandatory(true)
                                             .fixed(false)
                                             .metadata(metadataMapNewRelicMarkerIndices)
                                             .build();

    pipelineServiceImpl.populateParentFields(newRelicMarkerPipelineVar, NEWRELIC_MARKER_APPID, workflowVariables,
        "NewRelicMarkerAppId", pseWorkflowVariables);
    assertThat(newRelicMarkerPipelineVar.getMetadata().get(Variable.PARENT_FIELDS)).isNotNull();
    parents = (Map<String, String>) newRelicMarkerPipelineVar.getMetadata().get(Variable.PARENT_FIELDS);
    assertThat(parents.get("analysisServerConfigId")).isEqualTo("newRelicMarkerconfigId");

    pipelineServiceImpl.populateParentFields(
        newRelicMarkerPipelineVar, NEWRELIC_MARKER_APPID, null, "NewRelicMarkerAppId", pseWorkflowVariables);
    assertThat(newRelicMarkerPipelineVar.getMetadata().get(Variable.PARENT_FIELDS)).isNotNull();
  }
}