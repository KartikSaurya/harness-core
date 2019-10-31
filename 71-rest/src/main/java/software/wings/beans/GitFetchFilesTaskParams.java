package software.wings.beans;

import io.harness.delegate.task.TaskParameters;
import lombok.Builder;
import lombok.Data;
import software.wings.beans.appmanifest.AppManifestKind;
import software.wings.service.impl.ContainerServiceParams;

import java.util.Map;

@Data
@Builder
public class GitFetchFilesTaskParams implements TaskParameters {
  private String accountId;
  private String appId;
  private String activityId;
  private boolean isFinalState;
  private AppManifestKind appManifestKind;
  private Map<String, GitFetchFilesConfig> gitFetchFilesConfigMap;
  private final ContainerServiceParams containerServiceParams;
}
