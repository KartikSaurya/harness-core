/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.notification.entities;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.Team;
import io.harness.annotation.StoreIn;
import io.harness.annotations.dev.OwnedBy;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.DbAliases;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@FieldNameConstants(innerTypeName = "TemplateKeys")
@Document("notificationTemplates")
@Entity("notificationTemplates")
@TypeAlias("notificationTemplate")
@StoreIn(DbAliases.NOTIFICATION)
@OwnedBy(PL)
public class NotificationTemplate {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("unique_identifier_team_idx")
                 .unique(true)
                 .field(TemplateKeys.identifier)
                 .field(TemplateKeys.team)
                 .build())
        .add(CompoundMongoIndex.builder().name("team_idx").field(TemplateKeys.team).build())
        .build();
  }
  @Id @org.mongodb.morphia.annotations.Id private String id;
  private String identifier;
  @CreatedDate private long createdAt;
  @Version private long version;
  @LastModifiedDate private long lastUpdatedAt;
  @Indexed private Team team;
  private byte[] file;
  private boolean harnessManaged;
}
