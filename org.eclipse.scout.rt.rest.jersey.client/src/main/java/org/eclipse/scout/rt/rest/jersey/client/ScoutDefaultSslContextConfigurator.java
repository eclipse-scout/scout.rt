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

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.client.ClientBuilder;

import org.eclipse.scout.rt.rest.client.IGlobalRestClientConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IGlobalRestClientConfigurator} implementation registering the default {@link SSLContext} as SSL context.<br>
 * This avoids using the Jersey default implementation which creates a new SSL context instance without considering the
 * global trust manager.
 *
 * @see org.glassfish.jersey.SslConfigurator#createSSLContext()
 * @see org.eclipse.scout.rt.server.commons.GlobalTrustManager
 */
public class ScoutDefaultSslContextConfigurator implements IGlobalRestClientConfigurator {

  private static final Logger LOG = LoggerFactory.getLogger(ScoutDefaultSslContextConfigurator.class);

  @Override
  public void configure(ClientBuilder clientBuilder) {
    try {
      clientBuilder.sslContext(SSLContext.getDefault());
    }
    catch (NoSuchAlgorithmException e) {
      LOG.warn("Failed to register default SSL context for REST client, message={}", e.getMessage(), e);
    }
  }
}
