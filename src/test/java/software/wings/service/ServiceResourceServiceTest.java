package software.wings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.wings.beans.AppContainer.AppContainerBuilder.anAppContainer;
import static software.wings.beans.ArtifactSource.ArtifactType.JAR;
import static software.wings.beans.ArtifactSource.ArtifactType.WAR;
import static software.wings.beans.Command.Builder.aCommand;
import static software.wings.beans.ConfigFile.DEFAULT_TEMPLATE_ID;
import static software.wings.beans.ExecCommandUnit.Builder.anExecCommandUnit;
import static software.wings.beans.Graph.Builder.aGraph;
import static software.wings.beans.Graph.Link.Builder.aLink;
import static software.wings.beans.Graph.Node.Builder.aNode;
import static software.wings.beans.Graph.ORIGIN_STATE;
import static software.wings.beans.SearchFilter.Operator.EQ;
import static software.wings.beans.Service.Builder.aService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Verifier;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import software.wings.WingsBaseTest;
import software.wings.beans.Application;
import software.wings.beans.Command;
import software.wings.beans.CommandUnitType;
import software.wings.beans.ConfigFile;
import software.wings.beans.Graph;
import software.wings.beans.SearchFilter;
import software.wings.beans.Service;
import software.wings.beans.Service.Builder;
import software.wings.dl.PageRequest;
import software.wings.dl.WingsPersistence;
import software.wings.exception.WingsException;
import software.wings.service.intfc.ConfigService;
import software.wings.service.intfc.ServiceResourceService;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * Created by anubhaw on 5/4/16.
 */
public class ServiceResourceServiceTest extends WingsBaseTest {
  private final String SERVICE_ID = "SERVICE_ID";
  private final String APP_ID = "APP_ID";

  @Inject @Named("primaryDatastore") private Datastore datastore;

  @Mock private WingsPersistence wingsPersistence;

  @Mock private ConfigService configService;

  @Rule
  public Verifier verifier = new Verifier() {
    @Override
    protected void verify() throws Throwable {
      verifyNoMoreInteractions(configService, wingsPersistence);
    }
  };

  @Inject @InjectMocks private ServiceResourceService srs;

  private Builder builder = aService()
                                .withUuid(SERVICE_ID)
                                .withAppId(APP_ID)
                                .withName("SERVICE_NAME")
                                .withDescription("SERVICE_DESC")
                                .withArtifactType(JAR)
                                .withAppContainer(anAppContainer().withUuid("APP_CONTAINER_ID").build());

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    when(wingsPersistence.saveAndGet(eq(Service.class), any(Service.class))).thenReturn(builder.but().build());
    when(wingsPersistence.get(eq(Service.class), anyString(), anyString())).thenReturn(builder.but().build());
    when(wingsPersistence.createQuery(Service.class)).thenReturn(datastore.createQuery(Service.class));
    when(wingsPersistence.createUpdateOperations(Service.class))
        .thenReturn(datastore.createUpdateOperations(Service.class));
  }

  /**
   * Should list services.
   */
  @Test
  public void shouldListServices() {
    PageRequest<Service> request = new PageRequest<>();
    request.addFilter("appId", APP_ID, EQ);
    srs.list(request);
    ArgumentCaptor<PageRequest> argument = ArgumentCaptor.forClass(PageRequest.class);
    verify(wingsPersistence).query(eq(Service.class), argument.capture());
    SearchFilter filter = (SearchFilter) argument.getValue().getFilters().get(0);
    assertThat(filter.getFieldName()).isEqualTo("appId");
    assertThat(filter.getFieldValues()).containsExactly(APP_ID);
    assertThat(filter.getOp()).isEqualTo(EQ);
  }

  /**
   * Should save service.
   */
  @Test
  public void shouldSaveService() {
    Service service = srs.save(builder.but().build());
    assertThat(service.getUuid()).isEqualTo(SERVICE_ID);
    verify(wingsPersistence).addToList(Application.class, service.getAppId(), "services", service);
    verify(wingsPersistence).saveAndGet(Service.class, service);
  }

  /**
   * Should fetch service.
   */
  @Test
  public void shouldFetchService() {
    when(wingsPersistence.get(Service.class, APP_ID, SERVICE_ID)).thenReturn(builder.but().build());
    when(configService.getConfigFilesForEntity(DEFAULT_TEMPLATE_ID, SERVICE_ID))
        .thenReturn(new ArrayList<ConfigFile>());
    srs.get(APP_ID, SERVICE_ID);
    verify(wingsPersistence).get(Service.class, APP_ID, SERVICE_ID);
    verify(configService).getConfigFilesForEntity(DEFAULT_TEMPLATE_ID, SERVICE_ID);
  }

  /**
   * Should update service.
   */
  @Test
  public void shouldUpdateService() {
    Service service = builder.withName("UPDATED_SERVICE_NAME")
                          .withDescription("UPDATED_SERVICE_DESC")
                          .withArtifactType(WAR)
                          .withAppContainer(anAppContainer().withUuid("UPDATED_APP_CONTAINER_ID").build())
                          .build();
    srs.update(service);
    verify(wingsPersistence)
        .updateFields(Service.class, SERVICE_ID,
            ImmutableMap.of("name", "UPDATED_SERVICE_NAME", "description", "UPDATED_SERVICE_DESC", "artifactType", WAR,
                "appContainer", anAppContainer().withUuid("UPDATED_APP_CONTAINER_ID").build()));
    verify(wingsPersistence).get(Service.class, APP_ID, SERVICE_ID);
  }

  /**
   * Should delete service.
   */
  @Test
  public void shouldDeleteService() {
    srs.delete(APP_ID, SERVICE_ID);
    verify(wingsPersistence).delete(Service.class, SERVICE_ID);
  }

  /**
   * Should add command state.
   */
  @Test
  public void shouldAddCommandState() {
    when(wingsPersistence.addToList(
             eq(Service.class), eq(APP_ID), eq(SERVICE_ID), any(Query.class), eq("commands"), any(Command.class)))
        .thenReturn(true);

    Graph commandGraph =
        aGraph()
            .withGraphName("START")
            .addNodes(aNode().withId(ORIGIN_STATE).withType(ORIGIN_STATE).build(),
                aNode()
                    .withId("1")
                    .withType("EXEC")
                    .addProperty("commandPath", "/home/xxx/tomcat")
                    .addProperty("commandString", "bin/startup.sh")
                    .build())
            .addLinks(aLink().withFrom(ORIGIN_STATE).withTo("1").withType("ANY").withId("linkid").build())
            .build();

    Command command = aCommand()
                          .withName("START")
                          .withServiceId(SERVICE_ID)
                          .addCommandUnits(anExecCommandUnit()
                                               .withServiceId(SERVICE_ID)
                                               .withCommandPath("/home/xxx/tomcat")
                                               .withCommandString("bin/startup.sh")
                                               .build())
                          .build();
    srs.addCommand(APP_ID, SERVICE_ID, commandGraph);

    verify(wingsPersistence, times(2)).get(Service.class, APP_ID, SERVICE_ID);
    verify(wingsPersistence)
        .addToList(eq(Service.class), eq(APP_ID), eq(SERVICE_ID), any(Query.class), eq("commands"), eq(command));
    verify(wingsPersistence).createQuery(Service.class);
    verify(configService).getConfigFilesForEntity(DEFAULT_TEMPLATE_ID, SERVICE_ID);
  }

  /**
   * Should fail when command state is duplicate.
   */
  @Test
  public void shouldFailWhenCommandStateIsDuplicate() {
    when(wingsPersistence.addToList(
             eq(Service.class), eq(APP_ID), eq(SERVICE_ID), any(Query.class), eq("commands"), any(Command.class)))
        .thenReturn(false);

    Graph commandGraph =
        aGraph()
            .withGraphName("START")
            .addNodes(aNode().withId(ORIGIN_STATE).withType(ORIGIN_STATE).build(),
                aNode()
                    .withId("1")
                    .withType("EXEC")
                    .addProperty("commandPath", "/home/xxx/tomcat")
                    .addProperty("commandString", "bin/startup.sh")
                    .build())
            .addLinks(aLink().withFrom(ORIGIN_STATE).withTo("1").withType("ANY").withId("linkid").build())
            .build();

    Command command = aCommand()
                          .withName("START")
                          .withServiceId(SERVICE_ID)
                          .addCommandUnits(anExecCommandUnit()
                                               .withServiceId(SERVICE_ID)
                                               .withCommandPath("/home/xxx/tomcat")
                                               .withCommandString("bin/startup.sh")
                                               .build())
                          .build();
    assertThatExceptionOfType(WingsException.class).isThrownBy(() -> srs.addCommand(APP_ID, SERVICE_ID, commandGraph));

    verify(wingsPersistence).get(Service.class, APP_ID, SERVICE_ID);
    verify(wingsPersistence)
        .addToList(eq(Service.class), eq(APP_ID), eq(SERVICE_ID), any(Query.class), eq("commands"), eq(command));
    verify(wingsPersistence).createQuery(Service.class);
  }

  /**
   * Should delete command state.
   */
  @Test
  public void shouldDeleteCommandState() {
    srs.deleteCommand(APP_ID, SERVICE_ID, "START");

    verify(wingsPersistence, times(2)).get(Service.class, APP_ID, SERVICE_ID);
    verify(wingsPersistence).createUpdateOperations(Service.class);
    verify(wingsPersistence).createQuery(Service.class);
    verify(wingsPersistence).update(any(Query.class), any());
    verify(configService).getConfigFilesForEntity(DEFAULT_TEMPLATE_ID, SERVICE_ID);
  }

  /**
   * Should get command stencils.
   */
  @Test
  public void shouldGetCommandStencils() {
    when(wingsPersistence.get(eq(Service.class), anyString(), anyString()))
        .thenReturn(builder.but()
                        .addCommands(aCommand()
                                         .withName("START")
                                         .withServiceId(SERVICE_ID)
                                         .addCommandUnits(anExecCommandUnit()
                                                              .withServiceId(SERVICE_ID)
                                                              .withCommandPath("/home/xxx/tomcat")
                                                              .withCommandString("bin/startup.sh")
                                                              .build())
                                         .build())
                        .build());

    List<Object> commandStencils = srs.getCommandStencils(APP_ID, SERVICE_ID);

    assertThat(commandStencils)
        .isNotNull()
        .hasSize(1)
        .contains(ImmutableMap.of("name", "START", "type", CommandUnitType.COMMAND));

    verify(wingsPersistence, times(1)).get(Service.class, APP_ID, SERVICE_ID);
    verify(configService).getConfigFilesForEntity(DEFAULT_TEMPLATE_ID, SERVICE_ID);
  }
}
