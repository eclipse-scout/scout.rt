/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import static java.util.Collections.enumeration;
import static org.eclipse.scout.rt.platform.util.EnumerationUtility.*;
import static org.eclipse.scout.rt.platform.util.StringUtility.equalsIgnoreCase;

import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * {@link HttpServletRequestWrapper} to add additional headers for incoming requests.
 */
public class AdditionalHeadersHttpServletRequestWrapper extends HttpServletRequestWrapper {

  private final Map<String, String> m_additionalHeaders;

  public AdditionalHeadersHttpServletRequestWrapper(HttpServletRequest request, Map<String, String> additionalHeaders) {
    super(request);
    m_additionalHeaders = additionalHeaders;
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
