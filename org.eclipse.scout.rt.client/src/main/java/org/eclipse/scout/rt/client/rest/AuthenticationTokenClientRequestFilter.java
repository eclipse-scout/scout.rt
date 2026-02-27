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

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.authentication.DefaultAuthToken;
import org.eclipse.scout.rt.shared.authentication.DefaultAuthTokenSigner;

@Bean
public class AuthenticationTokenClientRequestFilter implements ClientRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) {
    addSignatureHeader(requestContext);
  }

  protected void addSignatureHeader(ClientRequestContext requestContext) {
    DefaultAuthToken token = BEANS.get(DefaultAuthTokenSigner.class).createDefaultSignedToken(DefaultAuthToken.class);
    if (token != null) {
      requestContext.getHeaders().putSingle(DefaultAuthToken.HTTP_HEADER_NAME, token.toString());
    }
  }
}
