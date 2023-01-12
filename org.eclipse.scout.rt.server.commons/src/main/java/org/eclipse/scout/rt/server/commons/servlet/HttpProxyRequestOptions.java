/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.HashMap;
import java.util.Map;

/**
 * Options for a HTTP request via {@link HttpProxy}.
 */
public class HttpProxyRequestOptions {

  private final Map<String, String> m_customRequestHeaders;
  private IRewriteRule m_rewriteRule;

  public HttpProxyRequestOptions() {
    m_customRequestHeaders = new HashMap<>();
  }

  public HttpProxyRequestOptions withCustomRequestHeader(String name, String value) {
    m_customRequestHeaders.put(name, value);
    return this;
  }

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
