package io.harness.pms.listener.progress;

import static io.harness.pms.sdk.PmsSdkModuleUtils.SDK_SERVICE_NAME;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.progress.ProgressEvent;
import io.harness.pms.events.base.PmsAbstractMessageListener;
import io.harness.pms.sdk.core.execution.events.node.progress.ProgressEventHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;

@OwnedBy(HarnessTeam.PIPELINE)
@Singleton
public class ProgressEventMessageListener extends PmsAbstractMessageListener<ProgressEvent> {
  private final ProgressEventHandler progressEventHandler;

  @Inject
  public ProgressEventMessageListener(
      @Named(SDK_SERVICE_NAME) String serviceName, ProgressEventHandler progressEventHandler) {
    super(serviceName, ProgressEvent.class);
    this.progressEventHandler = progressEventHandler;
  }

  @Override
  public boolean processMessage(ProgressEvent event, Map<String, String> metadataMap, Long timestamp) {
    return progressEventHandler.handleEvent(event, metadataMap, timestamp);
  }
}
