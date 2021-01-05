package io.harness.pms.yaml;

import io.harness.pms.contracts.plan.YamlFieldBlob;
import io.harness.pms.serializer.json.JsonOrchestrationUtils;
import io.harness.pms.serializer.json.JsonSerializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class YamlField implements JsonSerializable {
  private static final Charset CHARSET = Charset.forName(StandardCharsets.UTF_8.name());

  String name;
  @NotNull YamlNode node;

  @JsonCreator
  public YamlField(@JsonProperty("name") String name, @JsonProperty("node") YamlNode node) {
    this.name = name;
    this.node = node;
  }

  public YamlField(YamlNode node) {
    this(null, node);
  }

  public YamlFieldBlob toFieldBlob() {
    YamlFieldBlob.Builder builder = YamlFieldBlob.newBuilder().setBlob(ByteString.copyFrom(toJson(), CHARSET));
    if (name != null) {
      builder.setName(name);
    }
    return builder.build();
  }

  public static YamlField fromFieldBlob(YamlFieldBlob fieldBlob) throws IOException {
    return JsonOrchestrationUtils.asObject(fieldBlob.getBlob().toString(CHARSET), YamlField.class);
  }
}
