package io.harness.terraform.request;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(CDP)
public class TerraformRefreshCommandRequest {
  List<String> targets;
  List<String> varFilePaths;
  String varParams; // Needed to send inline variable values in CG
  String uiLogs; // Needed in CG to prevent printing secrets
}
