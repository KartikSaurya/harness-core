package software.wings.helpers.ext.helm;

import io.harness.delegate.task.protocol.DelegateMetaInfo;
import io.harness.delegate.task.protocol.DelegateTaskNotifyResponseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.wings.beans.command.CommandExecutionResult.CommandExecutionStatus;
import software.wings.helpers.ext.helm.response.HelmCommandResponse;

/**
 * Created by anubhaw on 3/22/18.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HelmCommandExecutionResponse implements DelegateTaskNotifyResponseData {
  private DelegateMetaInfo delegateMetaInfo;
  private HelmCommandResponse helmCommandResponse;
  private String errorMessage;
  private CommandExecutionStatus commandExecutionStatus;
}
