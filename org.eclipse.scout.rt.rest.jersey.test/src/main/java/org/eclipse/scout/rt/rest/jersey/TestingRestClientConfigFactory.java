/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import org.apache.http.conn.HttpClientConnectionManager;
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

  public static final String PROP_CONNECTION_MANAGER = "jersey.client.httpClientConnectionManager";

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
      protected int getMaxConnectionsTotal() {
        return 1;
      }

      @Override
      protected int getMaxConnectionsPerRoute() {
        return 1;
      }
    });
  }
}
