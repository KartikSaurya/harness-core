package io.harness.ng.core;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.core.common.beans.KeyValuePair;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.NotEmpty;

@OwnedBy(PL)
@Data
@Builder
@FieldNameConstants(innerTypeName = "ResourceKeys")
public class Resource {
  @NotEmpty String type;
  @NotEmpty String identifier;
  @Singular List<KeyValuePair> labels;
}
