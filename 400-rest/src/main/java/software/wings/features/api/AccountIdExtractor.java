/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.features.api;

public interface AccountIdExtractor<T> {
  String getAccountId(T obj);
}
