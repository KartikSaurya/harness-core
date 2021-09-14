package io.harness.cvng.core.services.api;

import io.harness.cvng.beans.change.ChangeCategory;
import io.harness.cvng.beans.change.ChangeEventDTO;
import io.harness.cvng.core.beans.ChangeSummaryDTO;
import io.harness.cvng.core.beans.params.ServiceEnvironmentParams;

import java.time.Instant;
import java.util.List;

public interface ChangeEventService {
  Boolean register(ChangeEventDTO changeEventDTO);
  List<ChangeEventDTO> get(ServiceEnvironmentParams serviceEnvironmentParams, List<String> changeSourceIdentifiers,
      Instant startTime, Instant endTime, List<ChangeCategory> changeCategories);
  ChangeSummaryDTO getChangeSummary(ServiceEnvironmentParams serviceEnvironmentParams,
      List<String> changeSourceIdentifiers, Instant startTime, Instant endTime);
}
