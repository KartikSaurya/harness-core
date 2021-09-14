/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans.ce.depricated;

import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.persistence.AccountAccess;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UpdatedAtAware;
import io.harness.persistence.UuidAware;

import software.wings.beans.AwsCrossAccountAttributes;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(value = "ceCloudAccount", noClassnameStored = true)
@FieldNameConstants(innerTypeName = "CECloudAccountKeys")
public class CECloudAccountOld implements PersistentEntity, UuidAware, CreatedAtAware, UpdatedAtAware, AccountAccess {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("no_dup_account")
                 .unique(true)
                 .field(CECloudAccountKeys.accountId)
                 .field(CECloudAccountKeys.infraAccountId)
                 .field(CECloudAccountKeys.infraMasterAccountId)
                 .field(CECloudAccountKeys.masterAccountSettingId)
                 .build())
        .build();
  }
  @Id String uuid;
  String accountId;
  String accountArn;
  String accountName;
  String infraAccountId;
  String infraMasterAccountId; // master account id
  AccountStatus accountStatus;
  String masterAccountSettingId; // setting id of ce connectors
  AwsCrossAccountAttributes awsCrossAccountAttributes;

  long createdAt;
  long lastUpdatedAt;

  public enum AccountStatus { NOT_VERIFIED, CONNECTED, NOT_CONNECTED }
}
