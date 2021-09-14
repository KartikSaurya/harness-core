/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.audit.repositories;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import org.springframework.data.mongodb.core.query.Criteria;

@OwnedBy(PL)
public interface AuditYamlRepositoryCustom {
  void delete(Criteria criteria);
}
