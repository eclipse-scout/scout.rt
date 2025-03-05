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
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;

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

    Integer maxTotal = CONFIG.getPropertyValue(AsyncForceHttp2MaxConnectionsTotalProperty.class);
    if (maxTotal != null && maxTotal > 0) {
      builder.setMaxConnTotal(maxTotal);
    }
    Integer defaultMaxPerRoute = CONFIG.getPropertyValue(AsyncForceHttp2MaxConnectionsPerRouteProperty.class);
    if (defaultMaxPerRoute > 0) {
      builder.setMaxConnPerRoute(defaultMaxPerRoute);
    }
  }

  /**
   * <p>
   * Configuration property to define the default maximum connections per route property for the {@link ForceHttp2DefaultAsyncHttpClientManager}.
   * </p>
   */
  public static class AsyncForceHttp2MaxConnectionsPerRouteProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 2048;
    }

    @Override
    public String getKey() {
      return "scout.http.async.forceHttp2.maxConnectionsPerRoute";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the default maximum connections per route property for the default async force-http2 HTTP client.\n"
          + "Default value is %d.", getDefaultValue());
    }
  }

  /**
   * Configuration property to define the default maximum connections property for the {@link ForceHttp2DefaultAsyncHttpClientManager}.
   */
  public static class AsyncForceHttp2MaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 2048;
    }

    @Override
    public String getKey() {
      return "scout.http.async.forceHttp2.maxConnectionsTotal";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the default total maximum connections property for the default async force-http2 HTTP client.\n"
          + "The default value is %d.", getDefaultValue());
    }
  }
}
