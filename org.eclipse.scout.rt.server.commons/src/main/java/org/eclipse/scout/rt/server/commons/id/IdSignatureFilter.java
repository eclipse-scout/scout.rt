/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.id;

import static java.util.Collections.enumeration;
import static org.eclipse.scout.rt.platform.util.EnumerationUtility.*;
import static org.eclipse.scout.rt.platform.util.StringUtility.equalsIgnoreCase;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;

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

  protected SignatureHttpServletRequestWrapper createSignatureRequest(HttpServletRequest request) {
    return new SignatureHttpServletRequestWrapper(request);
  }

  public static class SignatureHttpServletRequestWrapper extends HttpServletRequestWrapper {
    protected final Map<String, String> m_additionalHeaders = Map.of(ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString());

    public SignatureHttpServletRequestWrapper(HttpServletRequest request) {
      super(request);
    }

    @Override
    public String getHeader(String name) {
      return m_additionalHeaders.entrySet().stream()
          .filter(entry -> equalsIgnoreCase(name, entry.getKey()))
          .map(Entry::getValue)
          .findFirst()
          .orElseGet(() -> super.getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      return asEnumeration(Stream.concat(
              asStream(super.getHeaders(name)),
              m_additionalHeaders.entrySet().stream()
                  .filter(entry -> equalsIgnoreCase(name, entry.getKey()))
                  .map(Entry::getValue))
          .iterator());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      return enumeration(Stream.concat(asStream(super.getHeaderNames()), m_additionalHeaders.keySet().stream()).collect(Collectors.toSet()));
    }
  }
}
