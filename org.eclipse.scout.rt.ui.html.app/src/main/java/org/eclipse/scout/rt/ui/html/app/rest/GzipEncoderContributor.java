/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.app.rest;

import jakarta.ws.rs.client.ClientBuilder;

import org.eclipse.scout.rt.client.rest.ScoutBackendRestClientHelper.IScoutBackendRestClientBuilderContributor;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.CompressServiceTunnelRequestProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.glassfish.jersey.message.GZipEncoder;

/**
 * see also org.eclipse.scout.rt.client.rest.TestingGzipEncoderContributor
 */
public class GzipEncoderContributor implements IScoutBackendRestClientBuilderContributor {

  @Override
  public void contribute(ClientBuilder clientBuilder) {
    if (CONFIG.getPropertyValue(CompressServiceTunnelRequestProperty.class)) {
      clientBuilder.register(GZipEncoder.class);
    }
  }
}
