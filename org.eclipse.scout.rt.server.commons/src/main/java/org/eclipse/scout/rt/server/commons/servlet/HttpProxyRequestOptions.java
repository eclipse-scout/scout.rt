/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for a HTTP request via {@link HttpProxy}.
 */
public class HttpProxyRequestOptions {

  private final Map<String, String> m_customRequestHeaders;
  private IRewriteRule m_rewriteRule;

  public HttpProxyRequestOptions() {
    m_customRequestHeaders = new LinkedHashMap<>(); // use sorted map to make result deterministic
  }

  /**
   * @param name
   *     Name of the custom HTTP header (case-insensitive) that should be applied to the proxied HTTP request.
   *     Existing headers with the same name are replaced by this value.
   * @param value
   *     The value of the custom HTTP header. If {@code null}, the header is removed from the proxied request.
   */
  public HttpProxyRequestOptions withCustomRequestHeader(String name, String value) {
    m_customRequestHeaders.put(name, value);
    return this;
  }

  /**
   * Returns a list of HTTP headers to be added to the proxied HTTP request. Existing headers with the same name are
   * removed. New headers are only added if the value is not {@code null}.
   */
  public Map<String, String> getCustomRequestHeaders() {
    return m_customRequestHeaders;
  }

  public HttpProxyRequestOptions withRewriteRule(IRewriteRule rewriteRule) {
    m_rewriteRule = rewriteRule;
    return this;
  }

  public IRewriteRule getRewriteRule() {
    return m_rewriteRule;
  }
}
