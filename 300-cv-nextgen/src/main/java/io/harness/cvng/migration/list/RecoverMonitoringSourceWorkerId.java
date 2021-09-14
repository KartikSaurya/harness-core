/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cvng.migration.list;

import static io.harness.persistence.HQuery.excludeAuthority;

import io.harness.cvng.core.entities.MonitoringSourcePerpetualTask;
import io.harness.cvng.core.entities.MonitoringSourcePerpetualTask.MonitoringSourcePerpetualTaskKeys;
import io.harness.cvng.migration.CVNGMigration;
import io.harness.cvng.migration.beans.ChecklistItem;
import io.harness.persistence.HPersistence;

import com.google.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
@Slf4j
public class RecoverMonitoringSourceWorkerId implements CVNGMigration {
  @Inject private HPersistence hPersistence;

  @Override
  public void migrate() {
    List<MonitoringSourcePerpetualTask> monitoringSourcePerpetualTasks =
        hPersistence.createQuery(MonitoringSourcePerpetualTask.class, excludeAuthority).asList();
    log.info("Trying to migrate {}", monitoringSourcePerpetualTasks);

    monitoringSourcePerpetualTasks.forEach(monitoringSourcePerpetualTask -> {
      log.info("Starting migration for {}", monitoringSourcePerpetualTask);
      UpdateOperations<MonitoringSourcePerpetualTask> updateOperations =
          hPersistence.createUpdateOperations(MonitoringSourcePerpetualTask.class);
      updateOperations.unset(MonitoringSourcePerpetualTaskKeys.dataCollectionWorkerId);
      updateOperations.unset(MonitoringSourcePerpetualTaskKeys.perpetualTaskId);
      UpdateResults updateResults = hPersistence.update(monitoringSourcePerpetualTask, updateOperations);
      log.info("Updated monitoring source {}, {}", monitoringSourcePerpetualTask, updateResults.getUpdatedCount());
    });
  }

  @Override
  public ChecklistItem whatHappensOnRollback() {
    return ChecklistItem.NA;
  }

  @Override
  public ChecklistItem whatHappensIfOldVersionIteratorPicksMigratedEntity() {
    return ChecklistItem.NA;
  }
}
