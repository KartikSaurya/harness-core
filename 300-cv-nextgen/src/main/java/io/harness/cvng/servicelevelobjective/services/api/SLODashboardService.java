/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.servicelevelobjective.services.api;

import io.harness.cvng.core.beans.params.PageParams;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.servicelevelobjective.SLORiskCountResponse;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardApiFilter;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardWidget;
import io.harness.ng.beans.PageResponse;

public interface SLODashboardService {
  PageResponse<SLODashboardWidget> getSloDashboardWidgets(
      ProjectParams projectParams, SLODashboardApiFilter filter, PageParams pageParams);

  SLORiskCountResponse getRiskCount(ProjectParams projectParams, SLODashboardApiFilter serviceLevelObjectiveFilter);
}
