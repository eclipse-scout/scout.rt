/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.scout.rt.rest.client.IGlobalRestClientConfigurator;
import org.glassfish.jersey.client.spi.InvocationBuilderListener;

/**
 * {@link IGlobalRestClientConfigurator} implementation registering a custom {@link InvocationBuilderListener}.
 */
public class ScoutInvocationBuilderConfigurator implements IGlobalRestClientConfigurator {

  @Override
  public void configure(ClientBuilder clientBuilder) {
    clientBuilder.register(ScoutInvocationBuilderListener.class);
  }
}
