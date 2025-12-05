/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.id;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.server.commons.servlet.AdditionalHeadersHttpServletRequestWrapper;

/**
 * This {@link Filter} adds the {@link #ID_SIGNATURE_HTTP_HEADER} to the {@link ServletRequest}, which will be used
 * later on to determine if {@link IId}s need to be signed (see {@link IdCodecFlag#SIGNATURE} for more details).
 */
public class IdSignatureFilter implements Filter {
  public static final String ID_SIGNATURE_HTTP_HEADER = "X-ScoutIdSignature";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = createSignatureRequest((HttpServletRequest) request);
    chain.doFilter(req, response);
  }

  protected HttpServletRequestWrapper createSignatureRequest(HttpServletRequest request) {
    return new AdditionalHeadersHttpServletRequestWrapper(request, Collections.singletonMap(ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString()));
  }
}
