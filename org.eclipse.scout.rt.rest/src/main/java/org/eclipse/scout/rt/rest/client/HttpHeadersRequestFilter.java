/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;

/**
 * This filter ensures that the following HTTP headers are set for all REST calls to any REST API:
 * <ul>
 * <li>{@link HttpHeaders#ACCEPT_LANGUAGE} (according to the current {@link NlsLocale})
 * <li>{@link CorrelationId#HTTP_HEADER_NAME} (according the the current {@link CorrelationId})
 * <li>Headers provided by {@link GlobalOpenTelemetry}, see {@link #putOpenTelemetryContext(ClientRequestContext)}</li>
 * </ul>
 */
public class HttpHeadersRequestFilter implements IGlobalRestRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) {
    putLocale(requestContext);
    putCorrelationId(requestContext);
    putOpenTelemetryContext(requestContext);
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

  protected void putOpenTelemetryContext(ClientRequestContext requestContext) {
    TextMapSetter<ClientRequestContext> setter = (carrier, key, value) -> {
      if (carrier != null) {
        carrier.getHeaders().putSingle(key, value);
      }
    };
    GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator()
        .inject(Context.current(), requestContext, setter);
  }
}
