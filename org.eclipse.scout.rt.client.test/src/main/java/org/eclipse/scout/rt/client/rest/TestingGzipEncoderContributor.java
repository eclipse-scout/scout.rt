/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.rest;

import jakarta.ws.rs.client.ClientBuilder;

import org.eclipse.scout.rt.client.rest.ScoutBackendRestClientHelper.IScoutBackendRestClientBuilderContributor;
import org.glassfish.jersey.message.GZipEncoder;

/**
 * Enable compression for tests.
 * <p>
 * see also org.eclipse.scout.rt.ui.html.app.rest.GzipEncoderContributor
 */
public class TestingGzipEncoderContributor implements IScoutBackendRestClientBuilderContributor {

  @Override
  public void contribute(ClientBuilder clientBuilder) {
    clientBuilder.register(GZipEncoder.class);
  }
}
