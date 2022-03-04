/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

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
