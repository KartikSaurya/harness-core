/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.graphql.datafetcher.artifact;

import static io.harness.rule.OwnerRule.MILAN;
import static io.harness.rule.OwnerRule.PRABU;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.exception.InvalidRequestException;
import io.harness.rule.Owner;

import software.wings.beans.artifact.Artifact;
import software.wings.dl.WingsPersistence;
import software.wings.graphql.datafetcher.DataFetcherUtils;
import software.wings.graphql.schema.query.QLPageQueryParameters;
import software.wings.graphql.schema.type.QLArtifactConnection;
import software.wings.graphql.schema.type.QLPageInfo;
import software.wings.graphql.schema.type.aggregation.QLIdFilter;
import software.wings.graphql.schema.type.aggregation.QLIdOperator;
import software.wings.graphql.schema.type.aggregation.QLNoOpSortCriteria;
import software.wings.graphql.schema.type.aggregation.artifact.QLArtifactFilter;

import com.google.inject.Inject;
import graphql.schema.DataFetchingEnvironment;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mongodb.morphia.query.FieldEnd;
import org.mongodb.morphia.query.Query;

public class ArtifactConnectionDataFetcherTest extends CategoryTest {
  private DataFetcherUtils utils = Mockito.mock(DataFetcherUtils.class);

  @InjectMocks @Inject private ArtifactConnectionDataFetcher dataFetcher;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock Query<Artifact> query;
  @Mock FieldEnd fieldEnd;
  @Inject @InjectMocks private ArtifactQueryHelper artifactQueryHelper;

  @Before
  public void setUp() {
    dataFetcher = Mockito.spy(new ArtifactConnectionDataFetcher());
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = MILAN)
  @Category(UnitTests.class)
  public void testDataFetcher() {
    QLPageInfo page = QLPageInfo.builder().offset(0).hasMore(true).limit(10).total(20).build();
    Query query = Mockito.mock(Query.class);

    Mockito.doReturn(query)
        .when(dataFetcher)
        .populateFilters(
            Matchers.any(WingsPersistence.class), Matchers.anyList(), Matchers.any(Class.class), Matchers.anyBoolean());
    when(utils.populate(Matchers.any(QLPageQueryParameters.class), Matchers.any(Query.class),
             Matchers.any(DataFetcherUtils.Controller.class)))
        .thenReturn(page);

    QLArtifactConnection connection = dataFetcher.fetchConnection(
        Matchers.anyList(), Matchers.any(QLPageQueryParameters.class), (List<QLNoOpSortCriteria>) Matchers.isNull());

    Mockito.verify(dataFetcher, times(1))
        .populateFilters(
            Matchers.any(WingsPersistence.class), Matchers.anyList(), Matchers.any(Class.class), Matchers.anyBoolean());
    Mockito.verify(utils, times(1))
        .populate(Matchers.any(QLPageQueryParameters.class), Matchers.any(Query.class),
            Matchers.any(DataFetcherUtils.Controller.class));
    assertThat(page.getLimit().intValue()).isEqualTo(10);
    assertThat(page.getTotal().intValue()).isEqualTo(20);
    assertThat(page.getOffset().intValue()).isEqualTo(0);
    assertThat(page.getHasMore()).isTrue();
  }

  @Test
  @Owner(developers = PRABU)
  @Category(UnitTests.class)
  public void testGenerateFilter() {
    doReturn("fieldValue").when(utils).getFieldValue(any(), any());
    doReturn("source").when(dataFetchingEnvironment).getSource();
    doReturn("accountid").when(utils).getAccountId(dataFetchingEnvironment);

    QLArtifactFilter artifactFilter = dataFetcher.generateFilter(dataFetchingEnvironment, "ArtifactSource", "value");
    assertThat(artifactFilter.getArtifactSource()).isNotNull();
    assertThat(artifactFilter.getArtifactSource().getOperator()).isEqualTo(QLIdOperator.EQUALS);
    assertThatThrownBy(() -> dataFetcher.generateFilter(dataFetchingEnvironment, "ArtifactStream", "value"))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  @Owner(developers = PRABU)
  @Category(UnitTests.class)
  public void testPopulateFilters() {
    List<QLArtifactFilter> artifactFilters = Arrays.asList(
        QLArtifactFilter.builder()
            .artifactSource(
                QLIdFilter.builder().operator(QLIdOperator.EQUALS).values(new String[] {"ArtifactSource"}).build())
            .artifact(QLIdFilter.builder().operator(QLIdOperator.NOT_IN).values(new String[] {"Artifact"}).build())
            .artifactStreamType(QLIdFilter.builder().operator(QLIdOperator.NOT_NULL).build())
            .build());

    ArgumentCaptor<String> field = ArgumentCaptor.forClass(String.class);
    doReturn(fieldEnd).when(query).field(field.capture());
    artifactQueryHelper.setQuery(artifactFilters, query);
    assertThat(field.getAllValues()).containsExactlyInAnyOrder("artifactStreamId", "_id", "artifactStreamType");
    verify(utils, times(3)).setIdFilter(any(), any());
  }
}
