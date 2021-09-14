/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.delegate.beans.connector.k8Connector;

import io.harness.exception.InvalidRequestException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class KubernetesCredentialDTODeserializer extends StdDeserializer<KubernetesCredentialDTO> {
  public KubernetesCredentialDTODeserializer() {
    super(io.harness.delegate.beans.connector.k8Connector.KubernetesCredentialDTO.class);
  }

  protected KubernetesCredentialDTODeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public KubernetesCredentialDTO deserialize(JsonParser jp, DeserializationContext deserializationContext)
      throws IOException {
    JsonNode parentJsonNode = jp.getCodec().readTree(jp);
    JsonNode typeNode = parentJsonNode.get("type");
    JsonNode authSpec = parentJsonNode.get("spec");

    KubernetesCredentialType type = getType(typeNode);
    KubernetesCredentialSpecDTO k8sCredentialSpec = null;

    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    if (type == KubernetesCredentialType.MANUAL_CREDENTIALS) {
      k8sCredentialSpec = mapper.readValue(authSpec.toString(), KubernetesClusterDetailsDTO.class);
    } else if (type == KubernetesCredentialType.INHERIT_FROM_DELEGATE) {
      if (authSpec != null && !authSpec.isNull()) {
        throw new InvalidRequestException("No spec should be provided with the inherit from delegate type");
      }
    }

    return KubernetesCredentialDTO.builder().kubernetesCredentialType(type).config(k8sCredentialSpec).build();
  }

  KubernetesCredentialType getType(JsonNode typeNode) {
    String typeValue = typeNode.asText();
    return KubernetesCredentialType.fromString(typeValue);
  }
}
