/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ng.core.environment.beans;

import io.harness.data.validator.EntityIdentifier;
import io.harness.data.validator.EntityName;
import io.harness.data.validator.Trimmed;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.core.common.beans.NGTag;
import io.harness.persistence.PersistentEntity;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.Wither;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Entity(value = "environmentsNG", noClassnameStored = true)
@FieldNameConstants(innerTypeName = "EnvironmentKeys")
@Document("environmentsNG")
@TypeAlias("io.harness.ng.core.environment.beans.Environment")
public class Environment implements PersistentEntity {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("unique_accountId_organizationIdentifier_projectIdentifier_envIdentifier")
                 .unique(true)
                 .field(EnvironmentKeys.accountId)
                 .field(EnvironmentKeys.orgIdentifier)
                 .field(EnvironmentKeys.projectIdentifier)
                 .field(EnvironmentKeys.identifier)
                 .build())
        .build();
  }
  @Wither @Id @org.mongodb.morphia.annotations.Id private String id;

  @Trimmed @NotEmpty private String accountId;
  @Trimmed @NotEmpty private String orgIdentifier;
  @Trimmed @NotEmpty private String projectIdentifier;

  @NotEmpty @EntityIdentifier private String identifier;
  @EntityName private String name;
  @Size(max = 1024) String description;
  @Size(max = 100) String color;
  @NotEmpty private EnvironmentType type;
  @Wither @Singular @Size(max = 128) private List<NGTag> tags;

  @Wither @CreatedDate Long createdAt;
  @Wither @LastModifiedDate Long lastModifiedAt;
  @Wither @Version Long version;
  @Builder.Default Boolean deleted = Boolean.FALSE;
}
