/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.client.IRestClientConfigFactory;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.rest.client.RestClientProperties.LoggerVerbosity;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ClosingApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating JAX-RS client {@link Configuration} objects using Jersey.
 */
public class JerseyClientConfigFactory implements IRestClientConfigFactory {

  private static final Logger LOG = LoggerFactory.getLogger(JerseyClientConfigFactory.class);

  @Override
  public ClientConfig createClientConfig() {
    ClientConfig clientConfig = new ClientConfig();
    initConnectionProvider(clientConfig);
    return clientConfig;
  }

  /**
   * This default implementation delegates to {@link IJerseyConnectorProviderFactory#createConnectorProvider()}
   */
  protected void initConnectionProvider(ClientConfig clientConfig) {
    clientConfig.connectorProvider(new ClosingApacheConnectorProvider());
    clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, createConnectionManager());
  }

  /**
   * Creates a preconfigured Apache HTTP {@link HttpClientConnectionManager}. This default implementation supports up to
   * 32 concurrent connections to one particular route and 128 in total.
   */
  protected HttpClientConnectionManager createConnectionManager() {
    String[] sslProtocols = StringUtility.split(System.getProperty("https.protocols"), "\\s*,\\s*");
    String[] sslCipherSuites = StringUtility.split(System.getProperty("https.cipherSuites"), "\\s*,\\s*");

    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
        (SSLSocketFactory) SSLSocketFactory.getDefault(),
        sslProtocols != null && sslProtocols.length > 0 ? sslProtocols : null,
        sslCipherSuites != null && sslCipherSuites.length > 0 ? sslCipherSuites : null,
        new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault()));

    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", sslConnectionSocketFactory)
            .build(),
        null, null, null, getKeepAliveTimeoutMillis(), TimeUnit.MILLISECONDS);
    connectionManager.setValidateAfterInactivity(1);

    final int maxTotal = getMaxConnectionsTotal();
    if (maxTotal > 0) {
      connectionManager.setMaxTotal(maxTotal);
    }

    final int defaultMaxPerRoute = getMaxConnectionsPerRoute();
    if (defaultMaxPerRoute > 0) {
      connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }

    return connectionManager;
  }

  /**
   * Max timeout in ms connections are kept open when idle (requires keep-alive support). Default is 30 minutes.
   */
  protected long getKeepAliveTimeoutMillis() {
    return TimeUnit.MINUTES.toMillis(30);
  }

  /**
   * Max number of total concurrent connections managed by the {@link HttpClientConnectionManager} returned by
   * {@link #createConnectionManager()}. Default is 128.
   */
  protected int getMaxConnectionsTotal() {
    return 128;
  }

  /**
   * Max number of concurrent connections per route managed by the {@link HttpClientConnectionManager} returned by
   * {@link #createConnectionManager()}. Default is 32.
   */
  protected int getMaxConnectionsPerRoute() {
    return 32;
  }

  @Override
  public Client buildClient(ClientBuilder clientBuilder) {
    postProcessClientBuilder(clientBuilder);
    return clientBuilder.build();
  }

  /**
   * Post-process {@link ClientBuilder} instance before building {@link Client}.
   */
  protected void postProcessClientBuilder(ClientBuilder clientBuilder) {
    initLoggingFeature(Assertions.assertType(clientBuilder.getConfiguration(), ClientConfig.class));
  }

  /**
   * Add configuration for Jersey {@link LoggingFeature} based on Scout logging properties.
   */
  protected void initLoggingFeature(ClientConfig clientConfig) {
    if (clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_NAME) != null) {
      clientConfig.property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME_CLIENT, clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_NAME));
    }
    if (clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_LEVEL) != null) {
      clientConfig.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, getLevel(clientConfig));
    }
    if (clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_VERBOSITY) != null) {
      clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, getVerbosity(clientConfig));
    }
    if (clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_MAX_ENTITY_SIZE) != null) {
      clientConfig.property(LoggingFeature.LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT, clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_MAX_ENTITY_SIZE));
    }
  }

  protected String getLevel(ClientConfig clientConfig) {
    Object level = clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_LEVEL);
    if (level instanceof Level) {
      return Level.class.cast(level).getName();
    }
    else if (level instanceof String) {
      return String.class.cast(level);
    }
    LOG.warn("Invalid value {} for property {}, using default log level {}", level, RestClientProperties.LOGGING_LOGGER_LEVEL, LoggingFeature.DEFAULT_LOGGER_LEVEL);
    return LoggingFeature.DEFAULT_LOGGER_LEVEL;
  }

  protected Verbosity getVerbosity(ClientConfig clientConfig) {
    LoggerVerbosity verbosity = Assertions.assertType(clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_VERBOSITY), LoggerVerbosity.class);
    switch (verbosity) {
      case HEADERS_ONLY:
        return Verbosity.HEADERS_ONLY;
      case PAYLOAD_ANY:
        return Verbosity.PAYLOAD_ANY;
      case PAYLOAD_TEXT:
        return Verbosity.PAYLOAD_TEXT;
      default:
        return LoggingFeature.DEFAULT_VERBOSITY;
    }
  }
}
