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

import jakarta.ws.rs.client.ClientBuilder;

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
