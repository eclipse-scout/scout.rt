/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.http;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportConnectionTimeToLiveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportKeepAliveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsTotalProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRedirectPostProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;
import org.eclipse.scout.rt.shared.http.retry.CustomHttpRequestRetryHandler;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStore;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;

/**
 * Factory to create the {@link ApacheHttpTransport} instances.
 */
public class ApacheHttpTransportFactory implements IHttpTransportFactory {

  @Override
  public HttpTransport newHttpTransport(IHttpTransportManager manager) {
    HttpClientBuilder builder = HttpClients.custom();

    installConfigurableProxySelector(builder);
    installMultiSessionCookieStore(builder);

    setConnectionKeepAliveAndRetrySettings(builder);

    HttpClientConnectionManager cm = createHttpClientConnectionManager(manager);
    if (cm != null) {
      builder.setConnectionManager(cm);
    }

    interceptNewHttpTransport(builder, manager);
    manager.interceptNewHttpTransport(new ApacheHttpTransportBuilder(builder, cm));

    return new ApacheHttpTransport(builder.build());
  }

  protected void setConnectionKeepAliveAndRetrySettings(HttpClientBuilder builder) {
    addConnectionKeepAliveSettings(builder);
    addRetrySettings(builder);
    addRedirectSettings(builder);
  }

  protected void addConnectionKeepAliveSettings(HttpClientBuilder builder) {
    final boolean keepAliveProp = CONFIG.getPropertyValue(ApacheHttpTransportKeepAliveProperty.class);
    if (keepAliveProp) {
      builder.setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE);
    }
    else {
      builder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
    }
  }

  protected void addRetrySettings(HttpClientBuilder builder) {
    final boolean retryOnNoHttpResponseException = CONFIG.getPropertyValue(ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty.class);
    final boolean retryOnSocketExceptionByConnectionReset = CONFIG.getPropertyValue(ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty.class);
    if (retryOnNoHttpResponseException || retryOnSocketExceptionByConnectionReset) {
      builder.setRetryHandler(new CustomHttpRequestRetryHandler(1, false, retryOnNoHttpResponseException, retryOnSocketExceptionByConnectionReset));
    }
    else {
      builder.setRetryHandler(new DefaultHttpRequestRetryHandler(1, false));
    }
  }

  protected void addRedirectSettings(HttpClientBuilder builder) {
    final boolean redirectPost = CONFIG.getPropertyValue(ApacheHttpTransportRedirectPostProperty.class);
    if (redirectPost) {
      builder.setRedirectStrategy(EnhancedLaxRedirectStrategy.INSTANCE);
    }
    else {
      builder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
    }
  }

  /**
   * Return the {@link HttpClientConnectionManager}. Return <code>null</code> to create it using the
   * {@link HttpClientBuilder}. Caution: Returning a custom connection manager overrides several properties of the
   * {@link HttpClientBuilder}.
   */
  protected HttpClientConnectionManager createHttpClientConnectionManager(IHttpTransportManager manager) {
    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", createPlainSocketFactory())
            .register("https", createSSLConnectionSocketFactory())
            .build(),
        null, null, null, CONFIG.getPropertyValue(ApacheHttpTransportConnectionTimeToLiveProperty.class), TimeUnit.MILLISECONDS);
    connectionManager.setValidateAfterInactivity(1);

    Integer maxTotal = CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsTotalProperty.class);
    if (maxTotal != null && maxTotal > 0) {
      connectionManager.setMaxTotal(maxTotal);
    }
    Integer defaultMaxPerRoute = CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsPerRouteProperty.class);
    if (defaultMaxPerRoute > 0) {
      connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }
    initMetrics(manager, connectionManager);
    return connectionManager;
  }

  /**
   * Initializes metrics for this connection manager.
   */
  protected void initMetrics(IHttpTransportManager manager, PoolingHttpClientConnectionManager connectionManager) {
    Meter meter = GlobalOpenTelemetry.get().getMeter(getClass().getName());
    BEANS.get(HttpClientMetricsHelper.class).initMetrics(meter, manager.getName(),
        connectionManager.getTotalStats()::getAvailable,
        connectionManager.getTotalStats()::getLeased,
        connectionManager.getTotalStats()::getMax);
  }

  protected SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
    String[] sslProtocols = StringUtility.split(System.getProperty("https.protocols"), "\\s*,\\s*");
    String[] sslCipherSuites = StringUtility.split(System.getProperty("https.cipherSuites"), "\\s*,\\s*");
    return new SSLConnectionSocketFactory(
        (SSLSocketFactory) SSLSocketFactory.getDefault(),
        sslProtocols != null && sslProtocols.length > 0 ? sslProtocols : null,
        sslCipherSuites != null && sslCipherSuites.length > 0 ? sslCipherSuites : null,
        new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault()));
  }

  protected PlainConnectionSocketFactory createPlainSocketFactory() {
    return PlainConnectionSocketFactory.getSocketFactory();
  }

  /**
   * Install an instance of the {@link ConfigurableProxySelector} to select proxies.
   */
  protected void installConfigurableProxySelector(HttpClientBuilder builder) {
    builder.setRoutePlanner(new SystemDefaultRoutePlanner(BEANS.get(ConfigurableProxySelector.class)));
  }

  /**
   * Install a {@link MultiSessionCookieStore} to store cookies by session.
   */
  protected void installMultiSessionCookieStore(HttpClientBuilder builder) {
    builder.setDefaultCookieStore(BEANS.get(ApacheMultiSessionCookieStore.class));
  }

  /**
   * Intercept the building of the new {@link HttpTransport}.
   */
  protected void interceptNewHttpTransport(HttpClientBuilder builder, IHttpTransportManager manager) {
    // nop
  }

  public static class ApacheHttpTransportBuilder implements IHttpTransportBuilder {
    private final HttpClientBuilder m_builder;
    private final HttpClientConnectionManager m_connectionManager;

    public ApacheHttpTransportBuilder(HttpClientBuilder builder, HttpClientConnectionManager connectionManager) {
      m_builder = builder;
      m_connectionManager = connectionManager;
    }

    public HttpClientBuilder getBuilder() {
      return m_builder;
    }

    public HttpClientConnectionManager getConnectionManager() {
      return m_connectionManager;
    }
  }
}
