/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.yaml.command;

import software.wings.beans.NameValuePair;
import software.wings.beans.command.AbstractCommandUnit;
import software.wings.beans.command.AbstractCommandUnit.Yaml;
import software.wings.yaml.BaseEntityYaml;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ServiceCommand and Command are merged in yaml to provide a simplistic user configuration experience.
 * @author rktummala on 11/09/17
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommandYaml extends BaseEntityYaml {
  private String commandUnitType;
  private boolean targetToAllEnv;
  private List<String> targetEnvs = new ArrayList<>();
  private List<AbstractCommandUnit.Yaml> commandUnits = new ArrayList<>();
  private String templateUri;
  private List<NameValuePair> templateVariables;

  @Builder
  public CommandYaml(String type, String harnessApiVersion, String commandUnitType, boolean targetToAllEnv,
      List<String> targetEnvs, List<Yaml> commandUnits, String templateUri, List<NameValuePair> templateVariables) {
    super(type, harnessApiVersion);
    this.commandUnitType = commandUnitType;
    this.targetToAllEnv = targetToAllEnv;
    this.targetEnvs = targetEnvs;
    this.commandUnits = commandUnits;
    this.templateUri = templateUri;
    this.templateVariables = templateVariables;
  }
}
