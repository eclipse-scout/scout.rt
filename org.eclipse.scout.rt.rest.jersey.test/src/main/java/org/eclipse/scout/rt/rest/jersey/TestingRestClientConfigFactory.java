/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;

import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.rest.jersey.client.JerseyClientConfigFactory;
import org.eclipse.scout.rt.rest.jersey.client.ScoutApacheConnector;
import org.glassfish.jersey.client.ClientConfig;

/**
 * Testing implementation of {@link JerseyClientConfigFactory} enabling to setup a custom (shared) http connection
 * manager instance used for all tests.
 */
@Replace
public class TestingRestClientConfigFactory extends JerseyClientConfigFactory {

  public static final String PROP_CONNECTION_MANAGER = "scout.testing.httpClientConnectionManager";

  @Override
  protected void initConnectionProvider(ClientConfig clientConfig) {
    clientConfig.connectorProvider((client, runtimeConfig) -> new ScoutApacheConnector(client, runtimeConfig) {
      @Override
      protected boolean isConnectionManagerShared() {
        return true;
      }

      /**
       * Creates a {@link HttpClientConnectionManager} that manages exactly one connection. This limitation helps
       * finding resource leaks (i.e. leased connections that are never put back to the pool).
       */
      @Override
      protected HttpClientConnectionManager createConnectionManager(Client client, Configuration config, SSLContext sslContext) {
        HttpClientConnectionManager httpClientConnectionManager = super.createConnectionManager(client, config, sslContext);
        // preserve connection manager as property of JAX-RS client instance for later use in tests
        client.property(PROP_CONNECTION_MANAGER, httpClientConnectionManager);
        return httpClientConnectionManager;
      }

      @Override
      protected int getMaxConnectionsTotal(Configuration config) {
        return 1;
      }

      @Override
      protected int getMaxConnectionsPerRoute(Configuration config) {
        return 1;
      }
    });
  }
}
