/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.migrations.all;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.migrations.Migration;
import io.harness.persistence.HIterator;

import software.wings.api.DeploymentType;
import software.wings.beans.Service;
import software.wings.beans.Service.ServiceKeys;
import software.wings.dl.WingsPersistence;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@OwnedBy(CDP)
public class PcfServiceDeploymentMigration implements Migration {
  @Inject private WingsPersistence wingsPersistence;

  @Override
  public void migrate() {
    log.info("Retrieving PCF Services");
    final DBCollection collection = wingsPersistence.getCollection(Service.class);
    BulkWriteOperation bulkWriteOperation = collection.initializeUnorderedBulkOperation();
    int i = 1;

    try (HIterator<Service> services = new HIterator<>(
             wingsPersistence.createQuery(Service.class).filter(ServiceKeys.artifactType, "PCF").fetch())) {
      while (services.hasNext()) {
        Service service = services.next();
        if (service.getDeploymentType() != null) {
          continue;
        }

        if (i % 1000 == 0) {
          bulkWriteOperation.execute();
          bulkWriteOperation = collection.initializeUnorderedBulkOperation();
          log.info("Pcf Service: {} updated", i);
        }
        ++i;

        bulkWriteOperation
            .find(wingsPersistence.createQuery(Service.class)
                      .filter(ServiceKeys.uuid, service.getUuid())
                      .getQueryObject())
            .updateOne(
                new BasicDBObject("$set", new BasicDBObject(ServiceKeys.deploymentType, DeploymentType.PCF.name())));
      }

      if (i % 1000 != 1) {
        bulkWriteOperation.execute();
      }

    } catch (Exception e) {
      log.warn("Something failed in PcfServiceDeploymentType Migration", e);
    }
  }
}
