/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.glassfish.jersey.apache.connector;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.client.spi.Connector;

/**
 * @see ClosingApacheConnector
 */
public class ClosingApacheConnectorProvider extends ApacheConnectorProvider {

  @Override
  public Connector getConnector(Client client, Configuration runtimeConfig) {
    return new ClosingApacheConnector(client, runtimeConfig);
  }
}
