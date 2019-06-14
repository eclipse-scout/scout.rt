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
package org.eclipse.scout.rt.rest.jersey.client;

import javax.ws.rs.core.Configuration;

import org.eclipse.scout.rt.rest.client.IRestClientConfigFactory;
import org.eclipse.scout.rt.rest.jersey.client.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

/**
 * Factory for creating JAX-RS client {@link Configuration} objects using Jersey.
 */
public class JerseyClientConfigFactory implements IRestClientConfigFactory {

  @Override
  public ClientConfig createClientConfig() {
    ClientConfig clientConfig = new ClientConfig();
    initConnectionProvider(clientConfig);
    return clientConfig;
  }

  protected void initConnectionProvider(ClientConfig clientConfig) {
    clientConfig.connectorProvider(new ApacheConnectorProvider());
  }
}
