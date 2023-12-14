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

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.HttpHeaders;

import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.nls.NlsLocale;

/**
 * This filter ensures that the following HTTP headers are set for all REST calls to any REST API:
 * <ul>
 * <li>{@link HttpHeaders#ACCEPT_LANGUAGE} (according to the current {@link NlsLocale})
 * <li>{@link CorrelationId#HTTP_HEADER_NAME} (according the the current {@link CorrelationId})
 * </ul>
 */
public class HttpHeadersRequestFilter implements IGlobalRestRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) {
    putLocale(requestContext);
    putCorrelationId(requestContext);
  }

  protected void putLocale(ClientRequestContext requestContext) {
    if (requestContext.getHeaders().get(HttpHeaders.ACCEPT_LANGUAGE) == null) {
      requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT_LANGUAGE, NlsLocale.get());
    }
  }

  protected void putCorrelationId(ClientRequestContext requestContext) {
    if (requestContext.getHeaders().get(CorrelationId.HTTP_HEADER_NAME) == null) {
      final String cid = CorrelationId.CURRENT.get();
      if (cid != null) {
        requestContext.getHeaders().putSingle(CorrelationId.HTTP_HEADER_NAME, cid);
      }
    }
  }
}
