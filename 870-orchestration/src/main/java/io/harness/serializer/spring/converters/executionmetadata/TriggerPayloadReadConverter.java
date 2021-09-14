/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.serializer.spring.converters.executionmetadata;

import io.harness.pms.contracts.triggers.TriggerPayload;
import io.harness.serializer.spring.ProtoReadConverter;

public class TriggerPayloadReadConverter extends ProtoReadConverter<TriggerPayload> {
  public TriggerPayloadReadConverter() {
    super(TriggerPayload.class);
  }
}
