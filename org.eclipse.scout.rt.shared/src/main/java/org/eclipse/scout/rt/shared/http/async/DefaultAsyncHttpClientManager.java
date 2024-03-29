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

import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.http.ApacheMultiSessionCookieStore;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportConnectionTimeToLiveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportKeepAliveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsTotalProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;
import org.eclipse.scout.rt.shared.http.retry.CustomHttpRequestRetryStrategy;

/**
 * <p>
 * Use {@link HttpAsyncClients#custom()} to initialize this client; default configuration is set influenced by some
 * config properties.
 * </p>
 *
 * @see AbstractAsyncHttpClientManager
 * @see ApacheHttpTransportKeepAliveProperty
 * @see ApacheHttpTransportMaxConnectionsPerRouteProperty
 * @see ApacheHttpTransportMaxConnectionsTotalProperty
 * @see ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty
 * @see ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty
 * @see ApacheMultiSessionCookieStore
 * @see ConfigurableProxySelector
 */
public class DefaultAsyncHttpClientManager extends AbstractAsyncHttpClientManager<HttpAsyncClientBuilder> {

  @Override
  protected HttpAsyncClientBuilder createBuilder() {
    return HttpAsyncClients.custom();
  }

  @Override
  protected void interceptCreateClient(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.newHttpTransport(IHttpTransportManager), unfortunately there is no common interface
    installConfigurableProxySelector(builder);
    installMultiSessionCookieStore(builder);

    setConnectionKeepAliveAndRetrySettings(builder);

    builder.setConnectionManager(createConnectionManager());
  }

  protected void installConfigurableProxySelector(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.installConfigurableProxySelector(HttpClientBuilder), unfortunately there is no common interface
    builder.setRoutePlanner(new SystemDefaultRoutePlanner(BEANS.get(ConfigurableProxySelector.class)));
  }

  protected void installMultiSessionCookieStore(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.installMultiSessionCookieStore(HttpClientBuilder), unfortunately there is no common interface
    builder.setDefaultCookieStore(BEANS.get(ApacheMultiSessionCookieStore.class));
  }

  protected void setConnectionKeepAliveAndRetrySettings(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.setConnectionKeepAliveAndRetrySettings(HttpClientBuilder), unfortunately there is no common interface
    addConnectionKeepAliveSettings(builder);
    addRetrySettings(builder);
    addRedirectSettings(builder);
  }

  protected void addConnectionKeepAliveSettings(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.addConnectionKeepAliveSettings(HttpClientBuilder), unfortunately there is no common interface
    final boolean keepAliveProp = CONFIG.getPropertyValue(ApacheHttpTransportKeepAliveProperty.class);
    if (keepAliveProp) {
      builder.setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE);
    }
    else {
      builder.setConnectionReuseStrategy(((request, response, context) -> false));
    }
  }

  protected void addRetrySettings(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.addRetrySettings(HttpClientBuilder), unfortunately there is no common interface
    final boolean retryOnNoHttpResponseException = CONFIG.getPropertyValue(ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty.class);
    final boolean retryOnSocketExceptionByConnectionReset = CONFIG.getPropertyValue(ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty.class);
    if (retryOnNoHttpResponseException || retryOnSocketExceptionByConnectionReset) {
      builder.setRetryStrategy(new CustomHttpRequestRetryStrategy(1, retryOnNoHttpResponseException, retryOnSocketExceptionByConnectionReset));
    }
    else {
      builder.setRetryStrategy(new DefaultHttpRequestRetryStrategy());
    }
  }

  protected void addRedirectSettings(HttpAsyncClientBuilder builder) {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.addRedirectSettings(HttpClientBuilder), unfortunately there is no common interface
    builder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
  }

  protected PoolingAsyncClientConnectionManager createConnectionManager() {
    // see very similar code in org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.createHttpClientConnectionManager(IHttpTransportManager), unfortunately there is no common interface
    PoolingAsyncClientConnectionManagerBuilder builder = PoolingAsyncClientConnectionManagerBuilder
        .create()
        // use system properties to ensure SSLContexts.getSystemDefault is called which uses our trust manager
        .useSystemProperties();

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

    interceptCreateConnectionManager(builder);
    return builder.build();
  }

  protected void interceptCreateConnectionManager(PoolingAsyncClientConnectionManagerBuilder builder) {
    // nop
  }

  @Override
  protected CloseableHttpAsyncClient createClient(HttpAsyncClientBuilder builder) {
    return builder.build();
  }
}
