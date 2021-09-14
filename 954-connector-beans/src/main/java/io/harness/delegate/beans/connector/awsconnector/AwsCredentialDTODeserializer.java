/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.beans.connector.awsconnector;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

@OwnedBy(HarnessTeam.DX)
public class AwsCredentialDTODeserializer extends StdDeserializer<AwsCredentialDTO> {
  public AwsCredentialDTODeserializer() {
    super(io.harness.delegate.beans.connector.awsconnector.AwsCredentialDTO.class);
  }

  protected AwsCredentialDTODeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public AwsCredentialDTO deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
    JsonNode parentJsonNode = jp.getCodec().readTree(jp);
    JsonNode typeNode = parentJsonNode.get("type");
    JsonNode authSpec = parentJsonNode.get("spec");
    JsonNode crossAccNode = parentJsonNode.get("crossAccountAccess");

    AwsCredentialType type = getType(typeNode);
    AwsCredentialSpecDTO awsCredentialSpecDTO = null;
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();

    CrossAccountAccessDTO crossAccountAccessDTO = null;
    if (crossAccNode != null && !crossAccNode.isNull()) {
      crossAccountAccessDTO = mapper.readValue(crossAccNode.toString(), CrossAccountAccessDTO.class);
    }
    if (type == AwsCredentialType.MANUAL_CREDENTIALS) {
      awsCredentialSpecDTO = mapper.readValue(authSpec.toString(), AwsManualConfigSpecDTO.class);
    } else if (type == AwsCredentialType.INHERIT_FROM_DELEGATE || type == AwsCredentialType.IRSA) {
      if (authSpec != null && !authSpec.isNull()) {
        throw new InvalidRequestException("No spec should be provided with the inherit from delegate type");
      }
    }

    return AwsCredentialDTO.builder()
        .awsCredentialType(type)
        .config(awsCredentialSpecDTO)
        .crossAccountAccess(crossAccountAccessDTO)
        .build();
  }

  AwsCredentialType getType(JsonNode typeNode) {
    String typeValue = typeNode.asText();
    return AwsCredentialType.fromString(typeValue);
  }
}
