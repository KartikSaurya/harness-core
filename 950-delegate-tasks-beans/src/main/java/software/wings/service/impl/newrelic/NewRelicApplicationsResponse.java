/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service.impl.newrelic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Created by rsingh on 8/28/17.
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewRelicApplicationsResponse {
  private List<NewRelicApplication> applications;
}
