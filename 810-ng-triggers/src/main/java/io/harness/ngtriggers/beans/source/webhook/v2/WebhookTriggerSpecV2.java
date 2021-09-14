/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.ngtriggers.beans.source.webhook.v2;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.ngtriggers.Constants.AWS_CODECOMMIT_REPO;
import static io.harness.ngtriggers.Constants.BITBUCKET_REPO;
import static io.harness.ngtriggers.Constants.CUSTOM_REPO;
import static io.harness.ngtriggers.Constants.GITHUB_REPO;
import static io.harness.ngtriggers.Constants.GITLAB_REPO;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ngtriggers.beans.source.webhook.v2.awscodecommit.AwsCodeCommitSpec;
import io.harness.ngtriggers.beans.source.webhook.v2.bitbucket.BitbucketSpec;
import io.harness.ngtriggers.beans.source.webhook.v2.custom.CustomTriggerSpec;
import io.harness.ngtriggers.beans.source.webhook.v2.git.GitAware;
import io.harness.ngtriggers.beans.source.webhook.v2.git.PayloadAware;
import io.harness.ngtriggers.beans.source.webhook.v2.github.GithubSpec;
import io.harness.ngtriggers.beans.source.webhook.v2.gitlab.GitlabSpec;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = NAME, property = "type", include = EXTERNAL_PROPERTY, visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = GithubSpec.class, name = GITHUB_REPO)
  , @JsonSubTypes.Type(value = GitlabSpec.class, name = GITLAB_REPO),
      @JsonSubTypes.Type(value = BitbucketSpec.class, name = BITBUCKET_REPO),
      @JsonSubTypes.Type(value = AwsCodeCommitSpec.class, name = AWS_CODECOMMIT_REPO),
      @JsonSubTypes.Type(value = CustomTriggerSpec.class, name = CUSTOM_REPO)
})
@OwnedBy(PIPELINE)
public interface WebhookTriggerSpecV2 {
  GitAware fetchGitAware();
  PayloadAware fetchPayloadAware();
}
