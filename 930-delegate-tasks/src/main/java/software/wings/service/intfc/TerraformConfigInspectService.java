/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service.intfc;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;

import java.util.List;

@OwnedBy(CDP)
public interface TerraformConfigInspectService {
  List<String> parseFieldsUnderCategory(String directory, String category, boolean useLatestVersion);
}
