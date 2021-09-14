/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ccm.commons.entities.events;

import io.harness.annotation.StoreIn;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.FdTtlIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.DbAliases;
import io.harness.persistence.AccountAccess;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UuidAware;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;

@StoreIn(DbAliases.CENG)
@Data
@Entity(value = "publishedMessages", noClassnameStored = true)
@FieldNameConstants(innerTypeName = "PublishedMessageKeys")
@Slf4j
public final class PublishedMessage implements PersistentEntity, CreatedAtAware, UuidAware, AccountAccess {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("accountId_type_CreatedAt_occurredAt")
                 .field(PublishedMessageKeys.accountId)
                 .field(PublishedMessageKeys.type)
                 .field(PublishedMessageKeys.createdAt)
                 .field(PublishedMessageKeys.occurredAt)
                 .build())
        .build();
  }
  @Id private String uuid;
  private long createdAt;

  @EqualsAndHashCode.Exclude
  @Builder.Default
  @FdTtlIndex
  private Date validUntil = Date.from(OffsetDateTime.now().plusDays(14).toInstant());

  private final long occurredAt;
  private final String accountId;
  private final String type;
  private final byte[] data;
  private final String category;
  private final Map<String, String> attributes;

  @Setter(AccessLevel.NONE) private transient Message message;

  @Builder(toBuilder = true)
  private PublishedMessage(String uuid, String accountId, String type, byte[] data, Message message, String category,
      Map<String, String> attributes, long occurredAt) {
    this.uuid = uuid;
    this.accountId = accountId;
    this.type = type;
    this.data = data;
    this.message = message;
    this.category = category;
    this.attributes = attributes;
    this.occurredAt = occurredAt;
  }

  public Message getMessage() {
    if (message == null) {
      postLoad();
    }
    return message;
  }

  @PostLoad
  private void postLoad() {
    try {
      Any any = Any.parseFrom(data);
      @SuppressWarnings("unchecked") Class<? extends Message> clazz = (Class<? extends Message>) Class.forName(type);
      this.message = any.unpack(clazz);
    } catch (ClassNotFoundException | InvalidProtocolBufferException e) {
      log.error("message type is {} createdAt {} occuredAt {} attr {}", type, createdAt, occurredAt, attributes);
    }
  }
}
