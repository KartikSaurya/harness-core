/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.migration.timescale;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.migration.MigrationException;
import io.harness.migration.NGMigration;
import io.harness.migration.TimeScaleNotAvailableException;
import io.harness.timescaledb.TimeScaleDBService;

import com.google.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;

@Slf4j
@OwnedBy(HarnessTeam.DX)
public abstract class NGAbstractTimeScaleMigration implements NGMigration {
  @Inject TimeScaleDBService timeScaleDBService;

  private void runMigration(Connection connection, String name) throws Exception {
    InputStream inputstream = getClass().getClassLoader().getResourceAsStream(name);
    if (inputstream == null) {
      throw new MigrationException(
          String.format("[Migration]: Unable to run migration %s as script %s not found", getClass(), name));
    }
    InputStreamReader inputStreamReader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
    ScriptRunner scriptRunner = new ScriptRunner(connection);
    scriptRunner.setStopOnError(true);
    scriptRunner.runScript(inputStreamReader);
  }

  public abstract String getFileName();

  @Override
  public void migrate() {
    if (timeScaleDBService.isValid()) {
      try (Connection connection = timeScaleDBService.getDBConnection()) {
        runMigration(connection, getFileName());
      } catch (Exception e) {
        throw new MigrationException(String.format("[Migration]: Migration %s failed", getClass()), e);
      }
    } else {
      throw new TimeScaleNotAvailableException(
          String.format("[Migration]: Migration %s failed - TIMESCALEDBSERVICE NOT AVAILABLE", getClass()));
    }
  }
}
