/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.views.graphql;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ViewsQueryMetadata {
  SelectQuery query;
  List<QLCEViewFieldInput> fields;
}
