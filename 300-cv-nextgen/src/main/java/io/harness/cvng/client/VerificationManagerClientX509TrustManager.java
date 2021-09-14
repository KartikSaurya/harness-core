/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.cvng.client;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

class VerificationManagerClientX509TrustManager implements X509TrustManager {
  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[] {
        // internal network
    };
  }

  @Override
  public void checkClientTrusted(X509Certificate[] certs, String authType) {
    // internal network so no need to check
  }

  @Override
  public void checkServerTrusted(X509Certificate[] certs, String authType) {
    // internal network so no need to check
  }
}
