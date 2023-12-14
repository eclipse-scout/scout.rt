/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import java.util.logging.Level;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Configuration;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.rest.client.IRestClientConfigFactory;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.rest.client.RestClientProperties.LoggerVerbosity;
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
    return new ClientConfig();
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
    final ClientConfig clientConfig = Assertions.assertType(clientBuilder.getConfiguration(), ClientConfig.class);
    initLoggingFeature(clientConfig);
    initConnectionProvider(clientConfig);
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

  /**
   * Initializes connection provider based on configuration.
   */
  protected void initConnectionProvider(ClientConfig clientConfig) {
    clientConfig.connectorProvider(BEANS.get(ScoutApacheConnectorProvider.class));
  }

  protected String getLevel(ClientConfig clientConfig) {
    Object level = clientConfig.getProperty(RestClientProperties.LOGGING_LOGGER_LEVEL);
    if (level instanceof Level) {
      return ((Level) level).getName();
    }
    else if (level instanceof String) {
      return (String) level;
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
