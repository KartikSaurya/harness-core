package io.harness.gitsync.common.impl;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.OwnedBy;
import io.harness.gitsync.common.beans.GitToHarnessProgress;
import io.harness.gitsync.common.dtos.GitToHarnessProgressDTO;

import lombok.experimental.UtilityClass;

@UtilityClass
@OwnedBy(DX)
public class GitToHarnessProgressMapper {
  public GitToHarnessProgress toEntity(GitToHarnessProgressDTO gitToHarnessProgressDTO) {
    return GitToHarnessProgress.builder()
        .accountIdentifier(gitToHarnessProgressDTO.getAccountIdentifier())
        .branch(gitToHarnessProgressDTO.getBranch())
        .commitId(gitToHarnessProgressDTO.getCommitId())
        .eventType(gitToHarnessProgressDTO.getEventType())
        .gitFileChanges(gitToHarnessProgressDTO.getGitFileChanges())
        .processingResponse(gitToHarnessProgressDTO.getProcessingResponse())
        .repoUrl(gitToHarnessProgressDTO.getRepoUrl())
        .stepStartingTime(gitToHarnessProgressDTO.getStepStartingTime())
        .stepStatus(gitToHarnessProgressDTO.getStepStatus())
        .stepType(gitToHarnessProgressDTO.getStepType())
        .yamlChangeSetId(gitToHarnessProgressDTO.getYamlChangeSetId())
        .build();
  }

  public GitToHarnessProgressDTO writeDTO(GitToHarnessProgress gitToHarnessProgress) {
    return GitToHarnessProgressDTO.builder()
        .uuid(gitToHarnessProgress.getUuid())
        .accountIdentifier(gitToHarnessProgress.getAccountIdentifier())
        .branch(gitToHarnessProgress.getBranch())
        .commitId(gitToHarnessProgress.getCommitId())
        .eventType(gitToHarnessProgress.getEventType())
        .gitFileChanges(gitToHarnessProgress.getGitFileChanges())
        .processingResponse(gitToHarnessProgress.getProcessingResponse())
        .repoUrl(gitToHarnessProgress.getRepoUrl())
        .stepStartingTime(gitToHarnessProgress.getStepStartingTime())
        .stepStatus(gitToHarnessProgress.getStepStatus())
        .stepType(gitToHarnessProgress.getStepType())
        .yamlChangeSetId(gitToHarnessProgress.getYamlChangeSetId())
        .build();
  }
}
