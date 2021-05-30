package io.harness.gitsync.gittoharness;

import static io.harness.annotations.dev.HarnessTeam.DX;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.HarnessStringUtils.emptyIfNull;
import static io.harness.logging.AutoLogContext.OverrideBehavior.OVERRIDE_ERROR;

import io.harness.EntityType;
import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.gitsync.ChangeSet;
import io.harness.gitsync.ChangeSets;
import io.harness.gitsync.FileProcessingResponse;
import io.harness.gitsync.FileProcessingStatus;
import io.harness.gitsync.GitToHarnessInfo;
import io.harness.gitsync.GitToHarnessProcessRequest;
import io.harness.gitsync.ProcessingFailureStage;
import io.harness.gitsync.ProcessingResponse;
import io.harness.gitsync.dao.GitProcessingRequestService;
import io.harness.gitsync.interceptor.GitEntityInfo;
import io.harness.gitsync.interceptor.GitSyncBranchContext;
import io.harness.gitsync.interceptor.GitSyncConstants;
import io.harness.gitsync.interceptor.GitSyncThreadDecorator;
import io.harness.gitsync.logger.GitProcessingLogContext;
import io.harness.logging.AutoLogContext;
import io.harness.manage.GlobalContextManager;
import io.harness.manage.GlobalContextManager.GlobalContextGuard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.StringValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@OwnedBy(DX)
public class GitToHarnessSdkProcessorImpl implements GitToHarnessSdkProcessor {
  ChangeSetInterceptorService changeSetInterceptorService;
  GitSdkInterface changeSetHelperService;
  Supplier<List<EntityType>> sortOrder;
  GitSyncThreadDecorator gitSyncThreadDecorator;
  GitProcessingRequestService gitProcessingRequestDao;

  @Inject
  public GitToHarnessSdkProcessorImpl(ChangeSetInterceptorService changeSetInterceptorService,
      GitSdkInterface changeSetHelperService, @Named("GitSyncSortOrder") Supplier<List<EntityType>> sortOrder,
      GitSyncThreadDecorator gitSyncThreadDecorator, GitProcessingRequestService gitProcessingRequestDao) {
    this.changeSetInterceptorService = changeSetInterceptorService;
    this.changeSetHelperService = changeSetHelperService;
    this.sortOrder = sortOrder;
    this.gitSyncThreadDecorator = gitSyncThreadDecorator;
    this.gitProcessingRequestDao = gitProcessingRequestDao;
  }

  /**
   * Processing is a 4 step process.
   * <li><b>Preprocess step.</b> Generally a no op stage.</li>
   * <li><b>Sort step.</b> Changesets are sorted as per sort order.</li>
   * <li><b>Process step.</b> Change sets are processed by calling various service layers.</li>
   * <li><b>Post process step.</b> Collection of all the return data happens.</li>
   */
  @Override
  public ProcessingResponse gitToHarnessProcessingRequest(GitToHarnessProcessRequest gitToHarnessRequest) {
    ChangeSets changeSets = gitToHarnessRequest.getChangeSets();
    String accountId = gitToHarnessRequest.getAccountId();
    String commitId = gitToHarnessRequest.getCommitId().getValue();
    try (AutoLogContext ignore1 = new GitProcessingLogContext(accountId, commitId, OVERRIDE_ERROR)) {
      final Map<String, FileProcessingResponse> fileProcessingStatusMap =
          initializeProcessingResponse(gitToHarnessRequest);

      if (isEmpty(changeSets.getChangeSetList())) {
        return ProcessingResponse.newBuilder().setAccountId(gitToHarnessRequest.getAccountId()).build();
      }

      if (preProcessStage(changeSets, fileProcessingStatusMap, commitId, accountId)) {
        return flattenProcessingResponse(fileProcessingStatusMap, accountId, ProcessingFailureStage.RECEIVE_STAGE);
      }

      if (sortStage(changeSets, fileProcessingStatusMap, commitId, accountId)) {
        return flattenProcessingResponse(fileProcessingStatusMap, accountId, ProcessingFailureStage.SORT_STAGE);
      }
      if (processStage(changeSets, fileProcessingStatusMap, gitToHarnessRequest, commitId, accountId)) {
        return flattenProcessingResponse(fileProcessingStatusMap, accountId, ProcessingFailureStage.PROCESS_STAGE);
      }

      if (postProcessStage(changeSets, fileProcessingStatusMap, commitId, accountId)) {
        return flattenProcessingResponse(fileProcessingStatusMap, accountId, ProcessingFailureStage.PROCESS_STAGE);
      }

      return flattenProcessingResponse(fileProcessingStatusMap, accountId, null);
    }
  }

  private boolean postProcessStage(ChangeSets changeSets, Map<String, FileProcessingResponse> processingResponseMap,
      String commitId, String accountId) {
    try {
      final ProcessingResponse processingResponse = flattenProcessingResponse(processingResponseMap, accountId, null);
      changeSetInterceptorService.postChangeSetProcessing(processingResponse, accountId);
    } catch (Exception e) {
      return true;
    }
    return false;
  }

  private boolean processStage(ChangeSets changeSets, Map<String, FileProcessingResponse> processingResponseMap,
      GitToHarnessProcessRequest gitToHarnessRequest, String commitId, String accountId) {
    try {
      // todo(abhinav): Do parallel processing.
      for (ChangeSet changeSet : changeSets.getChangeSetList()) {
        final FileProcessingStatus status = processingResponseMap.get(changeSet.getFilePath()).getStatus();
        if (!status.equals(FileProcessingStatus.UNPROCESSED)) {
          continue;
        }
        try (GlobalContextGuard guard = GlobalContextManager.ensureGlobalContextGuard()) {
          GlobalContextManager.upsertGlobalContextRecord(
              createGitEntityInfo(gitToHarnessRequest.getGitToHarnessBranchInfo(), changeSet));
          changeSetHelperService.process(changeSet);
          updateFileProcessingResponse(
              FileProcessingStatus.SUCCESS, null, processingResponseMap, changeSet.getFilePath(), commitId, accountId);
        } catch (Exception e) {
          log.error("Exception {}", changeSet.getFilePath(), e);
          updateFileProcessingResponse(FileProcessingStatus.FAILURE, e.getMessage(), processingResponseMap,
              changeSet.getFilePath(), commitId, accountId);
        }
      }
    } catch (Exception e) {
      return true;
    }
    return false;
  }

  private GitSyncBranchContext createGitEntityInfo(GitToHarnessInfo gitToHarnessBranchInfo, ChangeSet changeSet) {
    String[] pathSplited = emptyIfNull(changeSet.getFilePath()).split(GitSyncConstants.FOLDER_PATH);
    if (pathSplited.length != 2) {
      throw new InvalidRequestException(
          String.format("The path %s doesn't contain the .harness folder, thus this file won't be processed",
              changeSet.getFilePath()));
    }
    String folderPath = pathSplited[0] + GitSyncConstants.FOLDER_PATH;
    String filePath = pathSplited[1];
    return GitSyncBranchContext.builder()
        .gitBranchInfo(GitEntityInfo.builder()
                           .branch(gitToHarnessBranchInfo.getBranch())
                           .folderPath(folderPath)
                           .filePath(filePath)
                           .yamlGitConfigId(gitToHarnessBranchInfo.getYamlGitConfigId())
                           .lastObjectId(changeSet.getObjectId() == null ? null : changeSet.getObjectId().getValue())
                           .isSyncFromGit(true)
                           .build())
        .build();
  }

  private boolean sortStage(ChangeSets changeSets, Map<String, FileProcessingResponse> processingResponseMap,
      String commitId, String accountId) {
    try {
      final List<ChangeSet> sortedChangeSets = sortChangeSets(sortOrder, changeSets.getChangeSetList());
      changeSetInterceptorService.postChangeSetSort(sortedChangeSets, accountId);
    } catch (Exception e) {
      updateFileProcessingResponseForAllChangeSets(
          processingResponseMap, FileProcessingStatus.SKIPPED, e.getMessage(), commitId, accountId);
      return true;
    }
    return false;
  }

  private boolean preProcessStage(ChangeSets changeSets, Map<String, FileProcessingResponse> processingResponseMap,
      String commitId, String accountId) {
    try {
      changeSetInterceptorService.onChangeSetReceive(changeSets, accountId);
    } catch (Exception e) {
      updateFileProcessingResponseForAllChangeSets(
          processingResponseMap, FileProcessingStatus.SKIPPED, e.getMessage(), commitId, accountId);
      return true;
    }
    return false;
  }

  private ProcessingResponse flattenProcessingResponse(Map<String, FileProcessingResponse> processingResponseMap,
      String accountId, ProcessingFailureStage processingFailureStage) {
    final List<FileProcessingResponse> fileProcessingResponses = new ArrayList<>(processingResponseMap.values());
    final ProcessingResponse.Builder processingResponseBuilder =
        ProcessingResponse.newBuilder().addAllResponse(fileProcessingResponses).setAccountId(accountId);
    if (processingFailureStage != null) {
      processingResponseBuilder.setProcessingFailureStage(processingFailureStage);
    }
    return processingResponseBuilder.build();
  }

  private void updateFileProcessingResponseForAllChangeSets(Map<String, FileProcessingResponse> gitProcessRequest,
      FileProcessingStatus status, String message, String commitId, String accountId) {
    gitProcessRequest.forEach(
        (key, value) -> updateFileProcessingResponse(status, message, gitProcessRequest, key, commitId, accountId));
  }

  private void updateFileProcessingResponse(FileProcessingStatus status, String message,
      Map<String, FileProcessingResponse> responseMap, String filePath, String commitId, String accountId) {
    gitProcessingRequestDao.updateFileStatus(commitId, filePath, status, message, accountId);
    final FileProcessingResponse responseValue = responseMap.get(filePath);
    final FileProcessingResponse.Builder fileProcessingResponseBuilder =
        FileProcessingResponse.newBuilder().setStatus(status).setFilePath(responseValue.getFilePath());
    if (!isEmpty(message)) {
      fileProcessingResponseBuilder.setErrorMsg(StringValue.of(message));
    }
    responseMap.put(filePath, fileProcessingResponseBuilder.build());
  }

  private Map<String, FileProcessingResponse> initializeProcessingResponse(
      GitToHarnessProcessRequest gitToHarnessProcessRequest) {
    return gitProcessingRequestDao.upsert(gitToHarnessProcessRequest);
  }

  private List<ChangeSet> sortChangeSets(Supplier<List<EntityType>> sortOrder, List<ChangeSet> changeSetList) {
    return changeSetList.stream().sorted(new ChangeSetSortComparator(sortOrder.get())).collect(Collectors.toList());
  }
}