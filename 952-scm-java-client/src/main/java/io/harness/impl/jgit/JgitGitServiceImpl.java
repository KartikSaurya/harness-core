package io.harness.impl.jgit;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.gitsync.GitFileDetails;
import io.harness.beans.gitsync.GitFilePathDetails;
import io.harness.delegate.beans.connector.scm.ScmConnector;
import io.harness.product.ci.scm.proto.CreateFileResponse;
import io.harness.product.ci.scm.proto.DeleteFileResponse;
import io.harness.product.ci.scm.proto.FileBatchContentResponse;
import io.harness.product.ci.scm.proto.FileContent;
import io.harness.product.ci.scm.proto.FindFilesInBranchResponse;
import io.harness.product.ci.scm.proto.FindFilesInCommitResponse;
import io.harness.product.ci.scm.proto.GetLatestCommitResponse;
import io.harness.product.ci.scm.proto.IsLatestFileResponse;
import io.harness.product.ci.scm.proto.ListBranchesResponse;
import io.harness.product.ci.scm.proto.ListCommitsResponse;
import io.harness.product.ci.scm.proto.UpdateFileResponse;
import io.harness.service.ScmClient;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({ @Inject }))
@Slf4j
@OwnedBy(DX)
public class JgitGitServiceImpl implements ScmClient {
  @Override
  public CreateFileResponse createFile(ScmConnector scmConnector, GitFileDetails gitFileDetails) {
    return null;
  }

  @Override
  public UpdateFileResponse updateFile(ScmConnector scmConnector, GitFileDetails gitFileDetails) {
    return null;
  }

  @Override
  public DeleteFileResponse deleteFile(ScmConnector scmConnector, GitFilePathDetails gitFilePathDetails) {
    return null;
  }

  @Override
  public FileContent getFileContent(ScmConnector scmConnector, GitFilePathDetails gitFilePathDetails) {
    return null;
  }

  @Override
  public FileBatchContentResponse getHarnessFilesOfBranch(
      ScmConnector connectorAssociatedWithGitSyncConfig, String branch) {
    return null;
  }

  @Override
  public FileContent getLatestFile(ScmConnector scmConnector, GitFilePathDetails gitFilePathDetails) {
    return null;
  }

  @Override
  public IsLatestFileResponse isLatestFile(
      ScmConnector scmConnector, GitFilePathDetails gitFilePathDetails, FileContent fileContent) {
    return null;
  }

  @Override
  public FileContent pushFile(ScmConnector scmConnector, GitFileDetails gitFileDetails) {
    return null;
  }

  @Override
  public FindFilesInBranchResponse findFilesInBranch(ScmConnector scmConnector, String branch) {
    return null;
  }

  @Override
  public FindFilesInCommitResponse findFilesInCommit(ScmConnector scmConnector, GitFilePathDetails gitFilePathDetails) {
    return null;
  }

  @Override
  public GetLatestCommitResponse getLatestCommit(ScmConnector scmConnector, String branch) {
    return null;
  }

  @Override
  public ListBranchesResponse listBranches(ScmConnector scmConnector) {
    return null;
  }

  @Override
  public ListCommitsResponse listCommits(ScmConnector scmConnector, String branch) {
    return null;
  }

  @Override
  public FileBatchContentResponse listFiles(ScmConnector connector, List<String> filePaths, String branch) {
    return null;
  }

  @Override
  public void createNewBranch(ScmConnector scmConnector, String branch, String defaultBranchName) {}
}
