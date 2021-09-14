/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.delegatetasks;

import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.govern.Switch.unhandled;
import static io.harness.k8s.K8sCommandUnitConstants.FetchFiles;
import static io.harness.logging.LogLevel.ERROR;
import static io.harness.logging.LogLevel.INFO;
import static io.harness.logging.LogLevel.WARN;

import static software.wings.beans.LogHelper.color;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
import io.harness.beans.FileContentBatchResponse;
import io.harness.data.structure.EmptyPredicate;
import io.harness.delegate.beans.DelegateTaskPackage;
import io.harness.delegate.beans.DelegateTaskResponse;
import io.harness.delegate.beans.connector.scm.ScmConnector;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.delegate.task.AbstractDelegateRunnableTask;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.git.GitFetchFilesTaskHelper;
import io.harness.delegate.task.scm.ScmDelegateClient;
import io.harness.git.model.GitFile;
import io.harness.logging.CommandExecutionStatus;
import io.harness.product.ci.scm.proto.FileContent;
import io.harness.product.ci.scm.proto.SCMGrpc;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.service.ScmServiceClient;

import software.wings.beans.GitConfig;
import software.wings.beans.GitFetchFilesConfig;
import software.wings.beans.GitFetchFilesTaskParams;
import software.wings.beans.GitFileConfig;
import software.wings.beans.LogColor;
import software.wings.beans.LogWeight;
import software.wings.beans.appmanifest.AppManifestKind;
import software.wings.beans.command.ExecutionLogCallback;
import software.wings.beans.yaml.GitCommandExecutionResponse;
import software.wings.beans.yaml.GitCommandExecutionResponse.GitCommandStatus;
import software.wings.beans.yaml.GitCommitResult;
import software.wings.beans.yaml.GitFetchFilesFromMultipleRepoResult;
import software.wings.beans.yaml.GitFetchFilesResult;
import software.wings.helpers.ext.k8s.request.K8sValuesLocation;
import software.wings.service.intfc.GitService;
import software.wings.service.intfc.security.EncryptionService;

import com.google.inject.Inject;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
@TargetModule(HarnessModule._930_DELEGATE_TASKS)
public class GitFetchFilesTask extends AbstractDelegateRunnableTask {
  @Inject private GitService gitService;
  @Inject private EncryptionService encryptionService;
  @Inject private DelegateLogService delegateLogService;
  @Inject private GitFetchFilesTaskHelper gitFetchFilesTaskHelper;
  @Inject private ScmDelegateClient scmDelegateClient;
  @Inject private ScmServiceClient scmServiceClient;
  @Inject private ScmFetchFilesHelper scmFetchFilesHelper;

  public static final int GIT_FETCH_FILES_TASK_ASYNC_TIMEOUT = 10;

  public GitFetchFilesTask(DelegateTaskPackage delegateTaskPackage, ILogStreamingTaskClient logStreamingTaskClient,
      Consumer<DelegateTaskResponse> consumer, BooleanSupplier preExecute) {
    super(delegateTaskPackage, logStreamingTaskClient, consumer, preExecute);
  }

  @Override
  public GitCommandExecutionResponse run(TaskParameters parameters) {
    GitFetchFilesTaskParams taskParams = (GitFetchFilesTaskParams) parameters;

    log.info("Running GitFetchFilesTask for account {}, app {}, activityId {}", taskParams.getAccountId(),
        taskParams.getAppId(), taskParams.getActivityId());

    String executionLogName = isEmpty(taskParams.getExecutionLogName()) ? FetchFiles : taskParams.getExecutionLogName();

    ExecutionLogCallback executionLogCallback = new ExecutionLogCallback(delegateLogService, taskParams.getAccountId(),
        taskParams.getAppId(), taskParams.getActivityId(), executionLogName);

    AppManifestKind appManifestKind = taskParams.getAppManifestKind();
    Map<String, GitFetchFilesResult> filesFromMultipleRepo = new HashMap<>();

    for (Entry<String, GitFetchFilesConfig> entry : taskParams.getGitFetchFilesConfigMap().entrySet()) {
      executionLogCallback.saveExecutionLog(
          color(format("%nFetching %s files from git for %s", getFileTypeMessage(appManifestKind), entry.getKey()),
              LogColor.White, LogWeight.Bold));

      GitFetchFilesConfig gitFetchFileConfig = entry.getValue();
      String k8ValuesLocation = entry.getKey();
      GitFetchFilesResult gitFetchFilesResult;

      try {
        gitFetchFilesResult =
            fetchFilesFromRepo(gitFetchFileConfig.getGitFileConfig(), gitFetchFileConfig.getGitConfig(),
                gitFetchFileConfig.getEncryptedDataDetails(), executionLogCallback, taskParams.isOptimizedFilesFetch());
      } catch (Exception ex) {
        String exceptionMsg = ex.getMessage();

        // Values.yaml in service spec is optional.
        if (AppManifestKind.VALUES == appManifestKind && K8sValuesLocation.Service.toString().equals(k8ValuesLocation)
            && ex.getCause() instanceof NoSuchFileException) {
          log.info("Values.yaml file not found. " + exceptionMsg, ex);
          executionLogCallback.saveExecutionLog(exceptionMsg, WARN);
          continue;
        }

        String msg = "Exception in processing GitFetchFilesTask. " + exceptionMsg;
        log.error(msg, ex);
        executionLogCallback.saveExecutionLog(msg, ERROR, CommandExecutionStatus.FAILURE);
        return GitCommandExecutionResponse.builder()
            .errorMessage(exceptionMsg)
            .gitCommandStatus(GitCommandStatus.FAILURE)
            .build();
      }

      filesFromMultipleRepo.put(entry.getKey(), gitFetchFilesResult);
    }

    if (taskParams.isFinalState()) {
      executionLogCallback.saveExecutionLog("\nDone.", INFO, CommandExecutionStatus.SUCCESS);
    }

    return GitCommandExecutionResponse.builder()
        .gitCommandResult(
            GitFetchFilesFromMultipleRepoResult.builder().filesFromMultipleRepo(filesFromMultipleRepo).build())
        .gitCommandStatus(GitCommandStatus.SUCCESS)
        .build();
  }

  private GitFetchFilesResult fetchFilesFromRepo(GitFileConfig gitFileConfig, GitConfig gitConfig,
      List<EncryptedDataDetail> encryptedDataDetails, ExecutionLogCallback executionLogCallback,
      boolean optimizedFilesFetch) {
    executionLogCallback.saveExecutionLog("Git connector Url: " + gitConfig.getRepoUrl());
    if (gitFileConfig.isUseBranch()) {
      executionLogCallback.saveExecutionLog("Branch: " + gitFileConfig.getBranch());
    } else {
      executionLogCallback.saveExecutionLog("CommitId: " + gitFileConfig.getCommitId());
    }

    List<String> filePathsToFetch = new ArrayList<>();
    if (EmptyPredicate.isNotEmpty(gitFileConfig.getTaskSpecFilePath())
        || EmptyPredicate.isNotEmpty(gitFileConfig.getServiceSpecFilePath())) {
      filePathsToFetch.add(gitFileConfig.getTaskSpecFilePath());
      if (!gitFileConfig.isUseInlineServiceDefinition()) {
        filePathsToFetch.add(gitFileConfig.getServiceSpecFilePath());
      }
      executionLogCallback.saveExecutionLog("\nFetching following Task and Service Spec files :");
      gitFetchFilesTaskHelper.printFileNamesInExecutionLogs(filePathsToFetch, executionLogCallback);
    } else if (EmptyPredicate.isNotEmpty(gitFileConfig.getFilePathList())) {
      filePathsToFetch = gitFileConfig.getFilePathList();
      executionLogCallback.saveExecutionLog("\nFetching following Files :");
      gitFetchFilesTaskHelper.printFileNamesInExecutionLogs(filePathsToFetch, executionLogCallback);
    } else {
      executionLogCallback.saveExecutionLog("\nFetching " + gitFileConfig.getFilePath());
      String filePath = isBlank(gitFileConfig.getFilePath()) ? "" : gitFileConfig.getFilePath();
      filePathsToFetch = Collections.singletonList(filePath);
    }

    GitFetchFilesResult gitFetchFilesResult;
    encryptionService.decrypt(gitConfig, encryptedDataDetails, false);
    if (scmFetchFilesHelper.shouldUseScm(optimizedFilesFetch, gitConfig)) {
      gitFetchFilesResult = fetchFilesFromRepoWithScm(gitFileConfig, gitConfig, filePathsToFetch, executionLogCallback);
    } else {
      gitFetchFilesResult = gitService.fetchFilesByPath(gitConfig, gitFileConfig.getConnectorId(),
          gitFileConfig.getCommitId(), gitFileConfig.getBranch(), filePathsToFetch, gitFileConfig.isUseBranch());
    }

    gitFetchFilesTaskHelper.printFileNamesInExecutionLogs(
        executionLogCallback, gitFetchFilesResult == null ? Collections.emptyList() : gitFetchFilesResult.getFiles());

    return gitFetchFilesResult;
  }

  private GitFetchFilesResult fetchFilesFromRepoWithScm(GitFileConfig gitFileConfig, GitConfig gitConfig,
      List<String> filePathList, ExecutionLogCallback executionLogCallback) {
    ScmConnector scmConnector = scmFetchFilesHelper.getScmConnector(gitConfig);
    FileContentBatchResponse fileBatchContentResponse;

    if (gitFileConfig.isUseBranch()) {
      fileBatchContentResponse = scmDelegateClient.processScmRequest(c
          -> scmServiceClient.listFilesByFilePaths(
              scmConnector, filePathList, gitFileConfig.getBranch(), SCMGrpc.newBlockingStub(c)));
    } else {
      fileBatchContentResponse = scmDelegateClient.processScmRequest(c
          -> scmServiceClient.listFilesByCommitId(
              scmConnector, filePathList, gitFileConfig.getCommitId(), SCMGrpc.newBlockingStub(c)));
    }

    List<GitFile> gitFiles =
        fileBatchContentResponse.getFileBatchContentResponse()
            .getFileContentsList()
            .stream()
            .filter(fileContent -> {
              if (fileContent.getStatus() != 200) {
                logFailedFileFetch(gitFileConfig, executionLogCallback, fileContent);
                return false;
              } else {
                return true;
              }
            })
            .map(fileContent
                -> GitFile.builder().fileContent(fileContent.getContent()).filePath(fileContent.getPath()).build())
            .collect(Collectors.toList());

    if (isNotEmpty(gitFiles)) {
      gitFiles.forEach(gitFile -> log.info("File fetched : " + gitFile.getFilePath()));
    }
    return GitFetchFilesResult.builder()
        .files(gitFiles)
        .gitCommitResult(GitCommitResult.builder()
                             .commitId(gitFileConfig.isUseBranch() ? "latest" : fileBatchContentResponse.getCommitId())
                             .build())
        .build();
  }

  private void logFailedFileFetch(
      GitFileConfig gitFileConfig, ExecutionLogCallback executionLogCallback, FileContent fileContent) {
    executionLogCallback.saveExecutionLog(
        new StringBuilder("Unable to fetch files for filePath [")
            .append(fileContent.getPath())
            .append("]")
            .append(gitFileConfig.isUseBranch() ? " for Branch: " : " for CommitId: ")
            .append(gitFileConfig.isUseBranch() ? gitFileConfig.getBranch() : gitFileConfig.getCommitId())
            .toString(),
        WARN);
  }

  @Override
  public GitCommandExecutionResponse run(Object[] parameters) {
    throw new NotImplementedException("not implemented");
  }

  private String getFileTypeMessage(AppManifestKind appManifestKind) {
    if (appManifestKind == null) {
      return "";
    }

    switch (appManifestKind) {
      case VALUES:
        return "Values";
      case PCF_OVERRIDE:
      case K8S_MANIFEST:
        return "manifest";

      default:
        unhandled(appManifestKind);
        return "";
    }
  }
}
