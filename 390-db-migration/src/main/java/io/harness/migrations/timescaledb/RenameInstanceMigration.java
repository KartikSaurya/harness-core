/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.migrations.timescaledb;

public class RenameInstanceMigration extends AbstractTimeScaleDBMigration {
  @Override
  public String getFileName() {
    return "timescaledb/rename_instance_table.sql";
  }
}
