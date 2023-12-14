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

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.TokenUtility;

public class BearerAuthRequestFilter implements ClientRequestFilter {

  private final char[] m_token;

  public BearerAuthRequestFilter(char[] token) {
    m_token = token;
  }

  protected char[] getToken() {
    return m_token;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    addTokenHeader(requestContext);
  }

  protected void addTokenHeader(ClientRequestContext requestContext) {
    requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + Base64Utility.encode(TokenUtility.toBytes(getToken())));
  }
}
