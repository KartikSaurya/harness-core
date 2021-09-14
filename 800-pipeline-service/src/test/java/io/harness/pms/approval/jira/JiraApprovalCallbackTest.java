/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.pms.approval.jira;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.rule.OwnerRule.BRIJESH;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.joor.Reflect.on;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.ErrorNotifyResponseData;
import io.harness.delegate.task.jira.JiraTaskNGResponse;
import io.harness.exception.HarnessJiraException;
import io.harness.exception.JiraStepException;
import io.harness.jira.JiraIssueNG;
import io.harness.logstreaming.ILogStreamingStepClient;
import io.harness.logstreaming.LogStreamingStepClientFactory;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.rule.Owner;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.approval.step.ApprovalInstanceService;
import io.harness.steps.approval.step.beans.ApprovalType;
import io.harness.steps.approval.step.jira.beans.CriteriaSpecWrapperDTO;
import io.harness.steps.approval.step.jira.beans.JexlCriteriaSpecDTO;
import io.harness.steps.approval.step.jira.beans.KeyValuesCriteriaSpecDTO;
import io.harness.steps.approval.step.jira.entities.JiraApprovalInstance;
import io.harness.steps.approval.step.jira.evaluation.CriteriaEvaluator;
import io.harness.tasks.BinaryResponseData;
import io.harness.tasks.ResponseData;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@OwnedBy(PIPELINE)
@PrepareForTest(CriteriaEvaluator.class)
public class JiraApprovalCallbackTest extends CategoryTest {
  @Mock private ApprovalInstanceService approvalInstanceService;
  @Mock private LogStreamingStepClientFactory logStreamingStepClientFactory;
  @Mock private KryoSerializer kryoSerializer;
  private static String approvalInstanceId = "approvalInstanceId";
  @Mock ILogStreamingStepClient iLogStreamingStepClient;
  @InjectMocks private JiraApprovalCallback jiraApprovalCallback;
  private static String accountId = "accountId";
  private static String orgIdentifier = "orgIdentifier";
  private static String projectIdentifier = "projectIdentifier";
  private static String pipelineIdentifier = "pipelineIdentifier";

  @Before
  public void setUp() {
    jiraApprovalCallback = spy(JiraApprovalCallback.builder().build());
    on(jiraApprovalCallback).set("approvalInstanceService", approvalInstanceService);
    on(jiraApprovalCallback).set("logStreamingStepClientFactory", logStreamingStepClientFactory);
    on(jiraApprovalCallback).set("kryoSerializer", kryoSerializer);
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testPush() {
    PowerMockito.mockStatic(CriteriaEvaluator.class);
    when(CriteriaEvaluator.evaluateCriteria(any(), any())).thenReturn(true);
    on(jiraApprovalCallback).set("approvalInstanceId", approvalInstanceId);
    Ambiance ambiance = Ambiance.newBuilder()
                            .putSetupAbstractions("accountId", accountId)
                            .putSetupAbstractions("orgIdentifier", orgIdentifier)
                            .putSetupAbstractions("projectIdentifier", projectIdentifier)
                            .putSetupAbstractions("pipelineIdentifier", pipelineIdentifier)
                            .build();
    JiraApprovalInstance instance = getJiraApprovalInstance(ambiance);
    Map<String, ResponseData> response = new HashMap<>();
    response.put("data", BinaryResponseData.builder().build());
    doReturn(JiraTaskNGResponse.builder().issue(new JiraIssueNG()).build())
        .when(kryoSerializer)
        .asInflatedObject(any());
    doReturn(iLogStreamingStepClient).when(logStreamingStepClientFactory).getLogStreamingStepClient(ambiance);
    doReturn(instance).when(approvalInstanceService).get(approvalInstanceId);
    jiraApprovalCallback.push(response);
    when(CriteriaEvaluator.evaluateCriteria(any(), any())).thenReturn(false);
    jiraApprovalCallback.push(response);
    JexlCriteriaSpecDTO rejectionCriteria = JexlCriteriaSpecDTO.builder().build();
    instance.setRejectionCriteria(CriteriaSpecWrapperDTO.builder().criteriaSpecDTO(rejectionCriteria).build());
    when(CriteriaEvaluator.evaluateCriteria(any(), eq(rejectionCriteria))).thenReturn(true);
    jiraApprovalCallback.push(response);
    rejectionCriteria.setExpression("a==a");
    jiraApprovalCallback.push(response);

    when(CriteriaEvaluator.evaluateCriteria(any(), eq(rejectionCriteria))).thenThrow(new JiraStepException("", true));
    jiraApprovalCallback.push(response);

    // Testing the case when approval criteria not available
    instance.setApprovalCriteria(null);
    assertThatThrownBy(() -> jiraApprovalCallback.push(response)).isInstanceOf(HarnessJiraException.class);

    doReturn(JiraTaskNGResponse.builder().build()).when(kryoSerializer).asInflatedObject(any());
    jiraApprovalCallback.push(response);
    // To test case of error in kryo serialization
    doReturn(ErrorNotifyResponseData.builder().build()).when(kryoSerializer).asInflatedObject(any());
    jiraApprovalCallback.push(response);
    // To throw exception while casting the response to ResponseData and catch the exception
    doReturn(null).when(kryoSerializer).asInflatedObject(any());
    jiraApprovalCallback.push(response);
  }

  private JiraApprovalInstance getJiraApprovalInstance(Ambiance ambiance) {
    JiraApprovalInstance instance =
        JiraApprovalInstance.builder()
            .approvalCriteria(
                CriteriaSpecWrapperDTO.builder().criteriaSpecDTO(KeyValuesCriteriaSpecDTO.builder().build()).build())
            .build();
    instance.setAmbiance(ambiance);
    instance.setType(ApprovalType.JIRA_APPROVAL);
    return instance;
  }
}
