/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.yaml;

import io.kubernetes.client.util.Yaml;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
/*
To be used in snakeyaml.Yaml constructor for quoting Y/y/N/n values in Yaml, while converting Java Object to Yaml
 */
public class BooleanPatchedRepresenter extends Yaml.CustomRepresenter {
  public BooleanPatchedRepresenter() {
    this.representers.put(String.class, new RepresentBooleanPatchedQuantity());
  }

  private class RepresentBooleanPatchedQuantity implements Represent {
    private RepresentBooleanPatchedQuantity() {}

    private boolean isBooleanInFormOfYOrN(String value) {
      List<String> booleanValues = new ArrayList<>(Arrays.asList("Y", "N"));
      return booleanValues.stream().anyMatch(value::equalsIgnoreCase);
    }

    public Node representData(Object data) {
      String quantity = (String) data;
      return isBooleanInFormOfYOrN(quantity)
          ? BooleanPatchedRepresenter.this.representScalar(Tag.STR, quantity, DumperOptions.ScalarStyle.SINGLE_QUOTED)
          : BooleanPatchedRepresenter.this.representScalar(Tag.STR, quantity, null);
    }
  }
}
