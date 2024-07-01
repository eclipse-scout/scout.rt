/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.id;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.client.AbstractRestClientHelper;

/**
 * This {@link ClientRequestFilter} adds the {@link #ID_SIGNATURE_HTTP_HEADER} to the {@link ClientRequestContext},
 * which will be used later on to determine if {@link IId}s need to be signed (see {@link IdCodecFlag#SIGNATURE} for
 * more details). This filter is not registered automatically, use
 * {@link AbstractRestClientHelper#getRequestFiltersToRegister()} in your rest client helper implementation to register
 * this filter.
 */
@Bean
@Priority(Priorities.HEADER_DECORATOR)
public class IdSignatureClientRequestFilter implements ClientRequestFilter {
  public static final String ID_SIGNATURE_HTTP_HEADER = "X-ScoutIdSignature";

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    addIdSignatureHeader(requestContext);
  }

  protected void addIdSignatureHeader(ClientRequestContext requestContext) {
    if (requestContext.getHeaders().get(ID_SIGNATURE_HTTP_HEADER) == null) {
      requestContext.getHeaders().putSingle(ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString());
    }
  }
}
