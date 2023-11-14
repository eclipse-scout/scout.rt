/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportConnectionTimeToLiveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportKeepAliveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsTotalProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;
import org.eclipse.scout.rt.shared.http.retry.CustomHttpRequestRetryStrategy;
import org.eclipse.scout.rt.shared.http.transport.ApacheHttpTransport;

import com.google.api.client.http.HttpTransport;

/**
 * Factory to create the {@link ApacheHttpTransport} instances.
 */
public class ApacheHttpTransportFactory implements IHttpTransportFactory {

  @Override
  public HttpTransport newHttpTransport(IHttpTransportManager manager) {
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.interceptCreateClient(HttpAsyncClientBuilder), unfortunately there is no common interface
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
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.setConnectionKeepAliveAndRetrySettings(HttpAsyncClientBuilder), unfortunately there is no common interface
    addConnectionKeepAliveSettings(builder);
    addRetrySettings(builder);
    addRedirectSettings(builder);
  }

  protected void addConnectionKeepAliveSettings(HttpClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.addConnectionKeepAliveSettings(HttpAsyncClientBuilder), unfortunately there is no common interface
    final boolean keepAliveProp = CONFIG.getPropertyValue(ApacheHttpTransportKeepAliveProperty.class);
    if (keepAliveProp) {
      builder.setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE);
    }
    else {
      builder.setConnectionReuseStrategy(((request, response, context) -> false));
    }
  }

  protected void addRetrySettings(HttpClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.addRetrySettings(HttpAsyncClientBuilder), unfortunately there is no common interface
    final boolean retryOnNoHttpResponseException = CONFIG.getPropertyValue(ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty.class);
    final boolean retryOnSocketExceptionByConnectionReset = CONFIG.getPropertyValue(ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty.class);
    if (retryOnNoHttpResponseException || retryOnSocketExceptionByConnectionReset) {
      builder.setRetryStrategy(new CustomHttpRequestRetryStrategy(1, retryOnNoHttpResponseException, retryOnSocketExceptionByConnectionReset));
    }
    else {
      builder.setRetryStrategy(new DefaultHttpRequestRetryStrategy());
    }
  }

  protected void addRedirectSettings(HttpClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.addRedirectSettings(HttpAsyncClientBuilder), unfortunately there is no common interface
    builder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
  }

  /**
   * Return the {@link HttpClientConnectionManager}. Return <code>null</code> to create it using the
   * {@link HttpClientBuilder}. Caution: Returning a custom connection manager overrides several properties of the
   * {@link HttpClientBuilder}.
   */
  protected HttpClientConnectionManager createHttpClientConnectionManager(IHttpTransportManager manager) {
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.createConnectionManager(), unfortunately there is no common interface
    PoolingHttpClientConnectionManagerBuilder builder = PoolingHttpClientConnectionManagerBuilder.create();

    builder.setSSLSocketFactory(createSSLConnectionSocketFactory());
    builder.setDefaultConnectionConfig(ConnectionConfig.custom()
        .setTimeToLive(CONFIG.getPropertyValue(ApacheHttpTransportConnectionTimeToLiveProperty.class), TimeUnit.MILLISECONDS)
        .setValidateAfterInactivity(1, TimeUnit.MILLISECONDS)
        .build());

    Integer maxTotal = CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsTotalProperty.class);
    if (maxTotal != null && maxTotal > 0) {
      builder.setMaxConnTotal(maxTotal);
    }
    Integer defaultMaxPerRoute = CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsPerRouteProperty.class);
    if (defaultMaxPerRoute > 0) {
      builder.setMaxConnPerRoute(defaultMaxPerRoute);
    }
    interceptNewHttpClientConnectionManager(builder, manager);

    return builder.build();
  }

  protected SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
    String[] sslProtocols = StringUtility.split(System.getProperty("https.protocols"), "\\s*,\\s*");
    String[] sslCipherSuites = StringUtility.split(System.getProperty("https.cipherSuites"), "\\s*,\\s*");
    return new SSLConnectionSocketFactory(
        (SSLSocketFactory) SSLSocketFactory.getDefault(),
        sslProtocols != null && sslProtocols.length > 0 ? sslProtocols : null,
        sslCipherSuites != null && sslCipherSuites.length > 0 ? sslCipherSuites : null,
        HttpsSupport.getDefaultHostnameVerifier());
  }

  protected PlainConnectionSocketFactory createPlainSocketFactory() {
    return PlainConnectionSocketFactory.getSocketFactory();
  }

  /**
   * Install an instance of the {@link ConfigurableProxySelector} to select proxies.
   */
  protected void installConfigurableProxySelector(HttpClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.installConfigurableProxySelector(HttpClientBuilder), unfortunately there is no common interface
    builder.setRoutePlanner(new SystemDefaultRoutePlanner(BEANS.get(ConfigurableProxySelector.class)));
  }

  /**
   * Install {@link ApacheMultiSessionCookieStore} to store cookies by session.
   */
  protected void installMultiSessionCookieStore(HttpClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager.installMultiSessionCookieStore(HttpAsyncClientBuilder), unfortunately there is no common interface
    builder.setDefaultCookieStore(BEANS.get(ApacheMultiSessionCookieStore.class));
  }

  /**
   * Intercept the building of the new {@link HttpTransport}.
   */
  protected void interceptNewHttpTransport(HttpClientBuilder builder, IHttpTransportManager manager) {
    // nop
  }

  /**
   * Intercept the building of the connection manager
   */
  protected void interceptNewHttpClientConnectionManager(PoolingHttpClientConnectionManagerBuilder builder, IHttpTransportManager manager) {
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
