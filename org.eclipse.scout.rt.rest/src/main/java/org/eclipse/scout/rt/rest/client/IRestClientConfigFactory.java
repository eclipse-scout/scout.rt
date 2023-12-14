/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Configuration;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Factory for creating JAX-RS implementor-specific {@link Configuration} and {@link Client} objects that are used to
 * build new {@link ClientBuilder} instances.
 *
 * @see ClientBuilder#withConfig(Configuration)
 */
@ApplicationScoped
public interface IRestClientConfigFactory {

  /**
   * @return new JAX-RS implementor-specific {@link Configuration}.
   */
  Configuration createClientConfig();

  /**
   * @return new JAX-RS implementor-specific {@link Client} based on given {@link ClientBuilder}
   */
  default Client buildClient(ClientBuilder clientBuilder) {
    return clientBuilder.build();
  }
}
