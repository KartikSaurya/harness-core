package io.harness.gitsync.gitsyncerror.service;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.gitsync.gitsyncerror.dtos.GitSyncErrorAggregateByCommitDTO;
import io.harness.gitsync.gitsyncerror.dtos.GitSyncErrorCountDTO;
import io.harness.gitsync.gitsyncerror.dtos.GitSyncErrorDTO;
import io.harness.ng.beans.PageRequest;
import io.harness.ng.beans.PageResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@OwnedBy(PL)
public interface GitSyncErrorService {
  PageResponse<GitSyncErrorAggregateByCommitDTO> listGitToHarnessErrorsGroupedByCommits(PageRequest pageRequest,
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String searchTerm, String repoId,
      String branch, Integer numberOfErrorsInSummary);

  PageResponse<GitSyncErrorDTO> listAllGitToHarnessErrors(PageRequest pageRequest, String accountIdentifier,
      String orgIdentifier, String projectIdentifier, String searchTerm, String repoId, String branch);

  PageResponse<GitSyncErrorDTO> listGitToHarnessErrorsForCommit(PageRequest pageRequest, String commitId,
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String repoId, String branch);

  Optional<GitSyncErrorDTO> save(GitSyncErrorDTO gitSyncErrorDTO);

  List<GitSyncErrorDTO> saveAll(List<GitSyncErrorDTO> gitSyncErrorDTOList);

  void overrideGitToHarnessErrors(String accountId, String repoUrl, String branchName, Set<String> filePaths);

  void resolveGitToHarnessErrors(
      String accountId, String repoUrl, String branchName, Set<String> filePaths, String commitId);

  Optional<GitSyncErrorDTO> getGitToHarnessError(
      String accountId, String commitId, String repoUrl, String branchName, String filePath);

  boolean deleteGitSyncErrors(List<String> errorIds, String accountId);

  void recordConnectivityError(String accountIdentifier, String repoUrl, String errorMessage);

  PageResponse<GitSyncErrorDTO> listConnectivityErrors(String accountIdentifier, String orgIdentifier,
      String projectIdentifier, String repoIdentifier, PageRequest pageRequest);

  GitSyncErrorCountDTO getErrorCount(String accountIdentifier, String orgIdentifier, String projectIdentifier,
      String searchTerm, String repoId, String branch);

  void resolveConnectivityErrors(String accountIdentifier, String repoUrl);
}
