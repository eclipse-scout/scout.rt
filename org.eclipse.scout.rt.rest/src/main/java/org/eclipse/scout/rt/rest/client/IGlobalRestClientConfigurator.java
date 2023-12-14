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

import jakarta.ws.rs.client.ClientBuilder;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Global configurator for {@link ClientBuilder}s used by {@link AbstractRestClientHelper}.
 */
@ApplicationScoped
public interface IGlobalRestClientConfigurator {

  void configure(ClientBuilder clientBuilder);
}
