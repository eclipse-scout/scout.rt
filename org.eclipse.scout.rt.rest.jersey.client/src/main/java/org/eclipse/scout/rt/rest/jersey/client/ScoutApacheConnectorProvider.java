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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;

import org.eclipse.scout.rt.platform.Bean;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

@Bean
public class ScoutApacheConnectorProvider implements ConnectorProvider {

  @Override
  public Connector getConnector(Client client, Configuration runtimeConfig) {
    return new ScoutApacheConnector(client, runtimeConfig);
  }
}
