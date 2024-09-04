/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.async;

import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;

/**
 * Extension of {@link DefaultAsyncHttpClientManager} to force HTTP/2 protocol.
 *
 * @see DefaultAsyncHttpClientManager
 */
public class ForceHttp2DefaultAsyncHttpClientManager extends DefaultAsyncHttpClientManager {

  @Override
  public String getName() {
    return "scout.transport.async.forceHttp2";
  }

  @Override
  protected void interceptCreateConnectionManager(PoolingAsyncClientConnectionManagerBuilder builder) {
    // this setting seems to be used also for non-encrypted connections
    builder.setDefaultTlsConfig(TlsConfig.custom().setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2).build());
  }
}
