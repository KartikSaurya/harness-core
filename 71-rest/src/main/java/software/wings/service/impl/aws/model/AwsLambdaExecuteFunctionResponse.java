package software.wings.service.impl.aws.model;

import io.harness.beans.ExecutionStatus;
import io.harness.delegate.task.protocol.DelegateMetaInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AwsLambdaExecuteFunctionResponse implements AwsResponse {
  private DelegateMetaInfo delegateMetaInfo;
  private ExecutionStatus executionStatus;
  private String errorMessage;
  private Integer statusCode;
  private String functionError;
  private String logResult;
  private String payload;
}