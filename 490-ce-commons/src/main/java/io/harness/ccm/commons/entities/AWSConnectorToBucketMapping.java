/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.commons.entities;

import static io.harness.annotations.dev.HarnessTeam.CE;

import io.harness.annotation.HarnessEntity;
import io.harness.annotation.StoreIn;
import io.harness.annotations.dev.OwnedBy;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.persistence.AccountAccess;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UpdatedAtAware;
import io.harness.persistence.UuidAware;

import com.github.reinert.jjschema.SchemaIgnore;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Data
@Builder
@FieldNameConstants(innerTypeName = "AWSConnectorToBucketMappingKeys")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(value = "awsEntityToBucketMapping", noClassnameStored = true)
@HarnessEntity(exportable = false)
@OwnedBy(CE)
@StoreIn("events")
public class AWSConnectorToBucketMapping
    implements PersistentEntity, UuidAware, AccountAccess, CreatedAtAware, UpdatedAtAware {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("unique_accountId_awsConnectorIdentifier")
                 .unique(true)
                 .field(AWSConnectorToBucketMappingKeys.accountId)
                 .field(AWSConnectorToBucketMappingKeys.awsConnectorIdentifier)
                 .build())
        .build();
  }
  @Id String uuid;
  String accountId;
  String awsConnectorIdentifier;
  String destinationBucket;
  @SchemaIgnore long createdAt;
  long lastUpdatedAt;
}
