/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.graphql.schema.type.secrets;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@OwnedBy(PL)
@Getter
@Setter
@Builder
@FieldNameConstants(innerTypeName = "QLEncryptedFileInputKeys")
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public class QLEncryptedFileInput {
  private String secretManagerId;
  private String name;
  private byte[] fileContent;
  private QLUsageScope usageScope;
  private boolean scopedToAccount;
  private boolean inheritScopesFromSM;
}
