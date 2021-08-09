package io.harness.states.codebase;

import static io.harness.rule.OwnerRule.ALEKSANDAR;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.execution.BranchWebhookEvent;
import io.harness.beans.execution.ManualExecutionSource;
import io.harness.beans.execution.PRWebhookEvent;
import io.harness.beans.execution.Repository;
import io.harness.beans.execution.WebhookBaseAttributes;
import io.harness.beans.execution.WebhookExecutionSource;
import io.harness.beans.sweepingoutputs.CodebaseSweepingOutput;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.ci.pod.ConnectorDetails;
import io.harness.delegate.beans.connector.scm.github.GithubConnectorDTO;
import io.harness.delegate.task.scm.GitRefType;
import io.harness.delegate.task.scm.ScmGitRefTaskParams;
import io.harness.delegate.task.scm.ScmGitRefTaskResponseData;
import io.harness.product.ci.scm.proto.Commit;
import io.harness.product.ci.scm.proto.FindPRResponse;
import io.harness.product.ci.scm.proto.ListCommitsInPRResponse;
import io.harness.product.ci.scm.proto.ListCommitsResponse;
import io.harness.product.ci.scm.proto.PullRequest;
import io.harness.product.ci.scm.proto.Reference;
import io.harness.product.ci.scm.proto.Signature;
import io.harness.product.ci.scm.proto.User;
import io.harness.rule.Owner;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.CI)
public class CodeBaseTaskStepTest extends CategoryTest {
  @InjectMocks CodeBaseTaskStep codeBaseTaskStep;
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldObtainTaskForBranchBuilds() {
    ManualExecutionSource executionSource = ManualExecutionSource.builder().branch("main").build();
    ConnectorDetails connectorDetails =
        ConnectorDetails.builder()
            .connectorConfig(GithubConnectorDTO.builder().url("http://github.com/octocat/").build())
            .build();

    ScmGitRefTaskParams taskParams =
        codeBaseTaskStep.obtainTaskParameters(executionSource, connectorDetails, "hello-world");
    assertThat(taskParams).isNotNull();
    assertThat(taskParams.getBranch()).isEqualTo("main");
    assertThat(taskParams.getScmConnector().getUrl()).isEqualTo("http://github.com/octocat/hello-world");
    assertThat(taskParams.getGitRefType()).isEqualTo(GitRefType.COMMIT);
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldObtainTaskForPRBuilds() {
    ManualExecutionSource executionSource = ManualExecutionSource.builder().prNumber("1").build();
    ConnectorDetails connectorDetails =
        ConnectorDetails.builder()
            .connectorConfig(GithubConnectorDTO.builder().url("http://github.com/octocat/hello-world").build())
            .build();

    ScmGitRefTaskParams taskParams = codeBaseTaskStep.obtainTaskParameters(executionSource, connectorDetails, null);
    assertThat(taskParams).isNotNull();
    assertThat(taskParams.getPrNumber()).isEqualTo(1);
    assertThat(taskParams.getScmConnector().getUrl()).isEqualTo("http://github.com/octocat/hello-world");
    assertThat(taskParams.getGitRefType()).isEqualTo(GitRefType.PULL_REQUEST_WITH_COMMITS);
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldBuildCommitShaCodebaseSweepingOutput() throws InvalidProtocolBufferException {
    ScmGitRefTaskResponseData scmGitRefTaskResponseData =
        ScmGitRefTaskResponseData.builder()
            .branch("main")
            .repoUrl("http://github.com/octocat/hello-world")
            .listCommitsResponse(ListCommitsResponse.newBuilder().addCommitIds("commitId").build().toByteArray())
            .build();
    CodebaseSweepingOutput codebaseSweepingOutput =
        codeBaseTaskStep.buildCommitShaCodebaseSweepingOutput(scmGitRefTaskResponseData);
    assertThat(codebaseSweepingOutput.getCommitSha()).isEqualTo("commitId");
    assertThat(codebaseSweepingOutput.getBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getRepoUrl()).isEqualTo("http://github.com/octocat/hello-world");
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldBuildPRCodebaseSweepingOutput() throws InvalidProtocolBufferException {
    ScmGitRefTaskResponseData scmGitRefTaskResponseData =
        ScmGitRefTaskResponseData.builder()
            .branch("main")
            .repoUrl("http://github.com/octocat/hello-world")
            .findPRResponse(FindPRResponse.newBuilder()
                                .setPr(PullRequest.newBuilder()
                                           .setTarget("main")
                                           .setSource("feature/abc")
                                           .setNumber(1)
                                           .setTitle("Title")
                                           .setSha("commitId")
                                           .setRef("ref")
                                           .setBase(Reference.newBuilder().setSha("commitIdBase").build())
                                           .setAuthor(User.newBuilder()
                                                          .setName("First Last")
                                                          .setEmail("first.last@email.com")
                                                          .setAvatar("http://...")
                                                          .setLogin("firstLast")
                                                          .build())
                                           .setLink("http://github.com/octocat/hello-world/pull/1")
                                           .setClosed(false)
                                           .setMerged(false)
                                           .build())
                                .build()
                                .toByteArray())
            .listCommitsInPRResponse(
                ListCommitsInPRResponse.newBuilder()
                    .addCommits(Commit.newBuilder()
                                    .setSha("commitId")
                                    .setMessage("message")
                                    .setLink("http://github.com/octocat/hello-world/pull/1/commits/commitId")
                                    .setCommitter(Signature.newBuilder()
                                                      .setDate(Timestamp.newBuilder().setSeconds(123123123).build())
                                                      .build())
                                    .setAuthor(Signature.newBuilder()
                                                   .setName("First Last")
                                                   .setEmail("first.last@email.com")
                                                   .setAvatar("http://...")
                                                   .setLogin("firstLast")
                                                   .build())
                                    .build())
                    .build()
                    .toByteArray())
            .build();
    CodebaseSweepingOutput codebaseSweepingOutput =
        codeBaseTaskStep.buildPRCodebaseSweepingOutput(scmGitRefTaskResponseData);
    assertThat(codebaseSweepingOutput.getBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getTargetBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getSourceBranch()).isEqualTo("feature/abc");
    assertThat(codebaseSweepingOutput.getPrNumber()).isEqualTo("1");
    assertThat(codebaseSweepingOutput.getPrTitle()).isEqualTo("Title");
    assertThat(codebaseSweepingOutput.getCommitSha()).isEqualTo("commitId");
    assertThat(codebaseSweepingOutput.getBaseCommitSha()).isEqualTo("commitIdBase");
    assertThat(codebaseSweepingOutput.getCommitRef()).isEqualTo("ref");
    assertThat(codebaseSweepingOutput.getRepoUrl()).isEqualTo("http://github.com/octocat/hello-world");
    assertThat(codebaseSweepingOutput.getGitUser()).isEqualTo("First Last");
    assertThat(codebaseSweepingOutput.getGitUserEmail()).isEqualTo("first.last@email.com");
    assertThat(codebaseSweepingOutput.getGitUserAvatar()).isEqualTo("http://...");
    assertThat(codebaseSweepingOutput.getGitUserId()).isEqualTo("firstLast");
    assertThat(codebaseSweepingOutput.getPullRequestLink()).isEqualTo("http://github.com/octocat/hello-world/pull/1");
    assertThat(codebaseSweepingOutput.getCommits().get(0))
        .isEqualTo(CodebaseSweepingOutput.CodeBaseCommit.builder()
                       .link("http://github.com/octocat/hello-world/pull/1/commits/commitId")
                       .id("commitId")
                       .message("message")
                       .timeStamp(123123123)
                       .ownerName("First Last")
                       .ownerEmail("first.last@email.com")
                       .ownerId("firstLast")
                       .build());
    assertThat(codebaseSweepingOutput.getState()).isEqualTo("open");
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldBuildManualCodebaseSweepingOutput() throws InvalidProtocolBufferException {
    ManualExecutionSource manualExecutionSource = ManualExecutionSource.builder().branch("main").build();
    CodebaseSweepingOutput codebaseSweepingOutput =
        codeBaseTaskStep.buildManualCodebaseSweepingOutput(manualExecutionSource);
    assertThat(codebaseSweepingOutput.getBranch()).isEqualTo("main");
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldBuildPRWebhookCodebaseSweepingOutput() {
    WebhookExecutionSource webhookExecutionSource =
        WebhookExecutionSource.builder()
            .webhookEvent(PRWebhookEvent.builder()
                              .sourceBranch("feature/abc")
                              .targetBranch("main")
                              .pullRequestId(1L)
                              .title("Title")
                              .pullRequestLink("http://github.com/octocat/hello-world/pull/1")
                              .baseAttributes(WebhookBaseAttributes.builder()
                                                  .after("commitId")
                                                  .before("commitIdBase")
                                                  .authorName("First Last")
                                                  .authorEmail("first.last@email.com")
                                                  .authorAvatar("http://...")
                                                  .authorLogin("firstLast")
                                                  .build())
                              .repository(Repository.builder().link("http://github.com/octocat/hello-world").build())
                              .build())
            .build();
    CodebaseSweepingOutput codebaseSweepingOutput =
        codeBaseTaskStep.buildWebhookCodebaseSweepingOutput(webhookExecutionSource);
    assertThat(codebaseSweepingOutput.getBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getTargetBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getSourceBranch()).isEqualTo("feature/abc");
    assertThat(codebaseSweepingOutput.getPrNumber()).isEqualTo("1");
    assertThat(codebaseSweepingOutput.getPrTitle()).isEqualTo("Title");
    assertThat(codebaseSweepingOutput.getCommitSha()).isEqualTo("commitId");
    assertThat(codebaseSweepingOutput.getBaseCommitSha()).isEqualTo("commitIdBase");
    assertThat(codebaseSweepingOutput.getRepoUrl()).isEqualTo("http://github.com/octocat/hello-world");
    assertThat(codebaseSweepingOutput.getGitUser()).isEqualTo("First Last");
    assertThat(codebaseSweepingOutput.getGitUserEmail()).isEqualTo("first.last@email.com");
    assertThat(codebaseSweepingOutput.getGitUserAvatar()).isEqualTo("http://...");
    assertThat(codebaseSweepingOutput.getGitUserId()).isEqualTo("firstLast");
    assertThat(codebaseSweepingOutput.getPullRequestLink()).isEqualTo("http://github.com/octocat/hello-world/pull/1");
  }

  @Test
  @Owner(developers = ALEKSANDAR)
  @Category(UnitTests.class)
  public void shouldBuildPushWebhookCodebaseSweepingOutput() {
    WebhookExecutionSource webhookExecutionSource =
        WebhookExecutionSource.builder()
            .webhookEvent(BranchWebhookEvent.builder()
                              .branchName("main")
                              .baseAttributes(WebhookBaseAttributes.builder()
                                                  .after("commitId")
                                                  .before("commitIdBase")
                                                  .authorName("First Last")
                                                  .authorEmail("first.last@email.com")
                                                  .authorAvatar("http://...")
                                                  .authorLogin("firstLast")
                                                  .build())
                              .repository(Repository.builder().link("http://github.com/octocat/hello-world").build())
                              .build())
            .build();
    CodebaseSweepingOutput codebaseSweepingOutput =
        codeBaseTaskStep.buildWebhookCodebaseSweepingOutput(webhookExecutionSource);
    assertThat(codebaseSweepingOutput.getBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getTargetBranch()).isEqualTo("main");
    assertThat(codebaseSweepingOutput.getCommitSha()).isEqualTo("commitId");
    assertThat(codebaseSweepingOutput.getRepoUrl()).isEqualTo("http://github.com/octocat/hello-world");
    assertThat(codebaseSweepingOutput.getGitUser()).isEqualTo("First Last");
    assertThat(codebaseSweepingOutput.getGitUserEmail()).isEqualTo("first.last@email.com");
    assertThat(codebaseSweepingOutput.getGitUserAvatar()).isEqualTo("http://...");
    assertThat(codebaseSweepingOutput.getGitUserId()).isEqualTo("firstLast");
  }
}