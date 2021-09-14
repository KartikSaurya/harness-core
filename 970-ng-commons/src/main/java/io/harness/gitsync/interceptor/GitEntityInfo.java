/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.gitsync.interceptor;

import static io.harness.annotations.dev.HarnessTeam.DX;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.gitsync.sdk.EntityGitDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.Wither;

@Getter
@Builder
@FieldNameConstants(innerTypeName = "GitEntityInfoKeys")
@JsonIgnoreProperties(ignoreUnknown = true)
@OwnedBy(DX)
public class GitEntityInfo {
  String branch;
  String yamlGitConfigId;
  @Setter String folderPath;
  @Setter String filePath;
  String commitMsg;
  String lastObjectId; // required in case of update file
  boolean isNewBranch;
  boolean isSyncFromGit;
  @Wither boolean findDefaultFromOtherRepos;
  String baseBranch;
  String commitId; // used for passing commitId in case of g2h.

  public boolean isNull() {
    // todo @Abhinav Maybe we should use null in place of default
    final String DEFAULT = "__default__";
    boolean isRepoNull = isEmpty(yamlGitConfigId) || yamlGitConfigId.equals(DEFAULT);
    boolean isBranchNull = isEmpty(branch) || branch.equals(DEFAULT);
    if (!isRepoNull && isBranchNull || isRepoNull && !isBranchNull) {
      throw new InvalidRequestException(String.format(
          "The repo should be provided with the branch, the request has repo %s, branch %s", yamlGitConfigId, branch));
    }
    return isRepoNull;
  }

  public EntityGitDetails toEntityGitDetails() {
    if (isNull()) {
      return null;
    }
    return EntityGitDetails.builder()
        .branch(branch)
        .repoIdentifier(yamlGitConfigId)
        .rootFolder(folderPath)
        .filePath(filePath)
        .build();
  }
}
