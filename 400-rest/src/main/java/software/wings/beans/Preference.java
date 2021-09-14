/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.beans;

import io.harness.annotation.HarnessEntity;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.persistence.AccountAccess;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.UtilityClass;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "preferenceType")
@JsonSubTypes({
  @Type(value = DeploymentPreference.class, name = "DEPLOYMENT_PREFERENCE")
  , @Type(value = AuditPreference.class, name = "AUDIT_PREFERENCE")
})
@FieldNameConstants(innerTypeName = "PreferenceKeys")

@EqualsAndHashCode(callSuper = false)
@Entity(value = "preferences")
@HarnessEntity(exportable = true)

public abstract class Preference extends Base implements AccountAccess {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("preference_index")
                 .field(PreferenceKeys.accountId)
                 .field(PreferenceKeys.userId)
                 .field(PreferenceKeys.name)
                 .build())
        .build();
  }

  @NotEmpty private String name;
  @NotEmpty private String accountId;
  @NotEmpty private String userId;
  private String preferenceType;

  public Preference(String preferenceType) {
    this.preferenceType = preferenceType;
  }

  @UtilityClass
  public static final class PreferenceKeys {
    // Temporary
    public static final String createdAt = "createdAt";
    public static final String uuid = "uuid";
  }
}
