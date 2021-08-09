package io.harness.states.codebase;

import static io.harness.beans.execution.ExecutionSource.Type.MANUAL;
import static io.harness.beans.execution.ExecutionSource.Type.WEBHOOK;
import static io.harness.beans.sweepingoutputs.CISweepingOutputNames.CODEBASE;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import static software.wings.beans.TaskType.SCM_GIT_REF_TASK;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.execution.BranchWebhookEvent;
import io.harness.beans.execution.ExecutionSource;
import io.harness.beans.execution.ManualExecutionSource;
import io.harness.beans.execution.PRWebhookEvent;
import io.harness.beans.execution.WebhookEvent;
import io.harness.beans.execution.WebhookExecutionSource;
import io.harness.beans.sweepingoutputs.CodebaseSweepingOutput;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.beans.ci.pod.ConnectorDetails;
import io.harness.delegate.beans.connector.scm.ScmConnector;
import io.harness.delegate.task.scm.GitRefType;
import io.harness.delegate.task.scm.ScmGitRefTaskParams;
import io.harness.delegate.task.scm.ScmGitRefTaskResponseData;
import io.harness.exception.ngexception.CIStageExecutionException;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.sdk.core.data.OptionalSweepingOutput;
import io.harness.pms.sdk.core.plan.creation.yaml.StepOutcomeGroup;
import io.harness.pms.sdk.core.resolver.RefObjectUtils;
import io.harness.pms.sdk.core.resolver.outputs.ExecutionSweepingOutputService;
import io.harness.pms.sdk.core.steps.executables.SyncExecutable;
import io.harness.pms.sdk.core.steps.executables.TaskExecutable;
import io.harness.pms.sdk.core.steps.io.PassThroughData;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.product.ci.scm.proto.Commit;
import io.harness.product.ci.scm.proto.FindPRResponse;
import io.harness.product.ci.scm.proto.ListCommitsInPRResponse;
import io.harness.product.ci.scm.proto.ListCommitsResponse;
import io.harness.product.ci.scm.proto.PullRequest;
import io.harness.serializer.KryoSerializer;
import io.harness.stateutils.buildstate.ConnectorUtils;
import io.harness.steps.StepUtils;
import io.harness.supplier.ThrowingSupplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
@OwnedBy(HarnessTeam.CI)
public class CodeBaseTaskStep implements TaskExecutable<CodeBaseTaskStepParameters, ScmGitRefTaskResponseData>,
                                         SyncExecutable<CodeBaseTaskStepParameters> {
  public static final StepType STEP_TYPE =
      StepType.newBuilder().setType("CI_CODEBASE_TASK").setStepCategory(StepCategory.STEP).build();

  @Inject private KryoSerializer kryoSerializer;
  @Inject private ConnectorUtils connectorUtils;
  @Inject private ExecutionSweepingOutputService executionSweepingOutputResolver;

  @Override
  public Class<CodeBaseTaskStepParameters> getStepParametersClass() {
    return CodeBaseTaskStepParameters.class;
  }

  @Override
  public TaskRequest obtainTask(
      Ambiance ambiance, CodeBaseTaskStepParameters stepParameters, StepInputPackage inputPackage) {
    ExecutionSource executionSource = stepParameters.getExecutionSource();
    if (executionSource.getType() != MANUAL) {
      throw new CIStageExecutionException("{} type is not supported in codebase delegate task for scm api operation");
    }

    ManualExecutionSource manualExecutionSource = (ManualExecutionSource) executionSource;
    ConnectorDetails connectorDetails =
        connectorUtils.getConnectorDetails(AmbianceUtils.getNgAccess(ambiance), stepParameters.getConnectorRef());

    ScmGitRefTaskParams scmGitRefTaskParams =
        obtainTaskParameters(manualExecutionSource, connectorDetails, stepParameters.getRepoName());

    final TaskData taskData = TaskData.builder()
                                  .async(true)
                                  .timeout(Duration.ofSeconds(30).toMillis())
                                  .taskType(SCM_GIT_REF_TASK.name())
                                  .parameters(new Object[] {scmGitRefTaskParams})
                                  .build();

    log.info("Created delegate task to fetch codebase info");
    return StepUtils.prepareTaskRequest(ambiance, taskData, kryoSerializer);
  }

  @Override
  public StepResponse handleTaskResult(Ambiance ambiance, CodeBaseTaskStepParameters stepParameters,
      ThrowingSupplier<ScmGitRefTaskResponseData> responseDataSupplier) throws Exception {
    ScmGitRefTaskResponseData scmGitRefTaskResponseData = null;
    try {
      log.info("Retrieving codebase info from returned delegate response");
      scmGitRefTaskResponseData = responseDataSupplier.get();
      log.info("Successfully retrieved codebase info from returned delegate response");
    } catch (Exception ex) {
      log.info("Failed to retrieve codebase info from returned delegate response");
      return StepResponse.builder().status(Status.FAILED).build();
    }

    CodebaseSweepingOutput codebaseSweepingOutput = null;
    if (scmGitRefTaskResponseData != null
        && scmGitRefTaskResponseData.getGitRefType() == GitRefType.PULL_REQUEST_WITH_COMMITS) {
      codebaseSweepingOutput = buildPRCodebaseSweepingOutput(scmGitRefTaskResponseData);
    } else if (scmGitRefTaskResponseData != null && scmGitRefTaskResponseData.getGitRefType() == GitRefType.COMMIT) {
      codebaseSweepingOutput = buildCommitShaCodebaseSweepingOutput(scmGitRefTaskResponseData);
    }
    if (codebaseSweepingOutput != null) {
      saveCodebaseSweepingOutput(ambiance, codebaseSweepingOutput);
    }

    return StepResponse.builder().status(Status.SUCCEEDED).build();
  }

  @Override
  public StepResponse executeSync(Ambiance ambiance, CodeBaseTaskStepParameters stepParameters,
      StepInputPackage inputPackage, PassThroughData passThroughData) {
    ExecutionSource executionSource = stepParameters.getExecutionSource();

    CodebaseSweepingOutput codebaseSweepingOutput = null;
    if (executionSource.getType() == MANUAL) {
      codebaseSweepingOutput = buildManualCodebaseSweepingOutput((ManualExecutionSource) executionSource);
    } else if (executionSource.getType() == WEBHOOK) {
      codebaseSweepingOutput = buildWebhookCodebaseSweepingOutput((WebhookExecutionSource) executionSource);
    }
    saveCodebaseSweepingOutput(ambiance, codebaseSweepingOutput);

    return StepResponse.builder().status(Status.SUCCEEDED).build();
  }

  @VisibleForTesting
  ScmGitRefTaskParams obtainTaskParameters(
      ManualExecutionSource manualExecutionSource, ConnectorDetails connectorDetails, String repoName) {
    ScmConnector scmConnector = (ScmConnector) connectorDetails.getConnectorConfig();
    String completeUrl = scmConnector.getUrl();
    if (isNotEmpty(repoName)) {
      completeUrl = StringUtils.stripEnd(scmConnector.getUrl(), "/") + "/" + StringUtils.stripStart(repoName, "/");
    }
    scmConnector.setUrl(completeUrl);

    String branch = manualExecutionSource.getBranch();
    String prNumber = manualExecutionSource.getPrNumber();
    if (isNotEmpty(branch)) {
      return ScmGitRefTaskParams.builder()
          .branch(branch)
          .gitRefType(GitRefType.COMMIT)
          .encryptedDataDetails(connectorDetails.getEncryptedDataDetails())
          .scmConnector(scmConnector)
          .build();
    } else if (isNotEmpty(prNumber)) {
      return ScmGitRefTaskParams.builder()
          .prNumber(Long.parseLong(prNumber))
          .gitRefType(GitRefType.PULL_REQUEST_WITH_COMMITS)
          .encryptedDataDetails(connectorDetails.getEncryptedDataDetails())
          .scmConnector((ScmConnector) connectorDetails.getConnectorConfig())
          .build();
    } else {
      throw new CIStageExecutionException("Manual codebase git task needs at least PR number or branch");
    }
  }

  @VisibleForTesting
  CodebaseSweepingOutput buildCommitShaCodebaseSweepingOutput(ScmGitRefTaskResponseData scmGitRefTaskResponseData)
      throws InvalidProtocolBufferException {
    CodebaseSweepingOutput codebaseSweepingOutput;
    final byte[] getLatestCommitResponseByteArray = scmGitRefTaskResponseData.getListCommitsResponse();
    if (isEmpty(getLatestCommitResponseByteArray)) {
      throw new CIStageExecutionException("Codebase git commit information can't be obtained");
    }
    ListCommitsResponse listCommitsResponse = ListCommitsResponse.parseFrom(getLatestCommitResponseByteArray);

    if (isEmpty(listCommitsResponse.getCommitIdsList())) {
      throw new CIStageExecutionException("Codebase git commit information can't be obtained");
    }
    codebaseSweepingOutput = CodebaseSweepingOutput.builder()
                                 .branch(scmGitRefTaskResponseData.getBranch())
                                 .commitSha(listCommitsResponse.getCommitIds(0))
                                 .repoUrl(scmGitRefTaskResponseData.getRepoUrl())
                                 .build();
    return codebaseSweepingOutput;
  }

  @VisibleForTesting
  CodebaseSweepingOutput buildWebhookCodebaseSweepingOutput(WebhookExecutionSource webhookExecutionSource) {
    if (webhookExecutionSource.getWebhookEvent().getType() == WebhookEvent.Type.PR) {
      PRWebhookEvent prWebhookEvent = (PRWebhookEvent) webhookExecutionSource.getWebhookEvent();
      return CodebaseSweepingOutput.builder()
          .branch(prWebhookEvent.getTargetBranch())
          .targetBranch(prWebhookEvent.getTargetBranch())
          .sourceBranch(prWebhookEvent.getSourceBranch())
          .prNumber(String.valueOf(prWebhookEvent.getPullRequestId()))
          .prTitle(prWebhookEvent.getTitle())
          .commitSha(prWebhookEvent.getBaseAttributes().getAfter())
          .baseCommitSha(prWebhookEvent.getBaseAttributes().getBefore())
          .repoUrl(prWebhookEvent.getRepository().getLink())
          .pullRequestLink(prWebhookEvent.getPullRequestLink())
          .gitUser(prWebhookEvent.getBaseAttributes().getAuthorName())
          .gitUserEmail(prWebhookEvent.getBaseAttributes().getAuthorEmail())
          .gitUserAvatar(prWebhookEvent.getBaseAttributes().getAuthorAvatar())
          .gitUserId(prWebhookEvent.getBaseAttributes().getAuthorLogin())
          .build();
    } else if (webhookExecutionSource.getWebhookEvent().getType() == WebhookEvent.Type.BRANCH) {
      BranchWebhookEvent branchWebhookEvent = (BranchWebhookEvent) webhookExecutionSource.getWebhookEvent();
      return CodebaseSweepingOutput.builder()
          .branch(branchWebhookEvent.getBranchName())
          .targetBranch(branchWebhookEvent.getBranchName())
          .commitSha(branchWebhookEvent.getBaseAttributes().getAfter())
          .repoUrl(branchWebhookEvent.getRepository().getLink())
          .gitUser(branchWebhookEvent.getBaseAttributes().getAuthorName())
          .gitUserEmail(branchWebhookEvent.getBaseAttributes().getAuthorEmail())
          .gitUserAvatar(branchWebhookEvent.getBaseAttributes().getAuthorAvatar())
          .gitUserId(branchWebhookEvent.getBaseAttributes().getAuthorLogin())
          .build();
    }
    return CodebaseSweepingOutput.builder().build();
  }

  @VisibleForTesting
  CodebaseSweepingOutput buildManualCodebaseSweepingOutput(ManualExecutionSource manualExecutionSource) {
    return CodebaseSweepingOutput.builder()
        .branch(manualExecutionSource.getBranch())
        .tag(manualExecutionSource.getTag())
        .commitSha(manualExecutionSource.getCommitSha())
        .build();
  }

  @VisibleForTesting
  CodebaseSweepingOutput buildPRCodebaseSweepingOutput(ScmGitRefTaskResponseData scmGitRefTaskResponseData)
      throws InvalidProtocolBufferException {
    CodebaseSweepingOutput codebaseSweepingOutput;
    final byte[] findPRResponseByteArray = scmGitRefTaskResponseData.getFindPRResponse();
    final byte[] listCommitsInPRResponseByteArray = scmGitRefTaskResponseData.getListCommitsInPRResponse();
    final String repoUrl = scmGitRefTaskResponseData.getRepoUrl();

    if (findPRResponseByteArray == null || listCommitsInPRResponseByteArray == null) {
      throw new CIStageExecutionException("Codebase git information can't be obtained");
    }

    FindPRResponse findPRResponse = FindPRResponse.parseFrom(findPRResponseByteArray);
    ListCommitsInPRResponse listCommitsInPRResponse =
        ListCommitsInPRResponse.parseFrom(listCommitsInPRResponseByteArray);
    PullRequest pr = findPRResponse.getPr();
    List<Commit> commits = listCommitsInPRResponse.getCommitsList();
    List<CodebaseSweepingOutput.CodeBaseCommit> codeBaseCommits = new ArrayList<>();
    for (Commit commit : commits) {
      codeBaseCommits.add(CodebaseSweepingOutput.CodeBaseCommit.builder()
                              .id(commit.getSha())
                              .message(commit.getMessage())
                              .link(commit.getLink())
                              .timeStamp(commit.getCommitter().getDate().getSeconds())
                              .ownerEmail(commit.getAuthor().getEmail())
                              .ownerId(commit.getAuthor().getLogin())
                              .ownerName(commit.getAuthor().getName())
                              .build());
    }

    codebaseSweepingOutput = CodebaseSweepingOutput.builder()
                                 .branch(pr.getTarget())
                                 .sourceBranch(pr.getSource())
                                 .targetBranch(pr.getTarget())
                                 .prNumber(String.valueOf(pr.getNumber()))
                                 .prTitle(pr.getTitle())
                                 .commitSha(pr.getSha())
                                 .baseCommitSha(pr.getBase().getSha())
                                 .commitRef(pr.getRef())
                                 .repoUrl(repoUrl) // Add repo url to scm.PullRequest and get it from there
                                 .gitUser(pr.getAuthor().getName())
                                 .gitUserAvatar(pr.getAuthor().getAvatar())
                                 .gitUserEmail(pr.getAuthor().getEmail())
                                 .gitUserId(pr.getAuthor().getLogin())
                                 .pullRequestLink(pr.getLink())
                                 .commits(codeBaseCommits)
                                 .state(getState(pr))
                                 .build();
    return codebaseSweepingOutput;
  }

  private void saveCodebaseSweepingOutput(Ambiance ambiance, CodebaseSweepingOutput codebaseSweepingOutput) {
    OptionalSweepingOutput optionalSweepingOutput =
        executionSweepingOutputResolver.resolveOptional(ambiance, RefObjectUtils.getOutcomeRefObject(CODEBASE));
    if (!optionalSweepingOutput.isFound()) {
      try {
        executionSweepingOutputResolver.consume(
            ambiance, CODEBASE, codebaseSweepingOutput, StepOutcomeGroup.PIPELINE.name());
      } catch (Exception e) {
        log.error("Error while consuming codebase sweeping output", e);
      }
    }
  }

  @NotNull
  private String getState(PullRequest pr) {
    String state = "open";
    if (pr.getClosed()) {
      state = "closed";
    } else if (pr.getMerged()) {
      state = "merged";
    }
    return state;
  }
}
