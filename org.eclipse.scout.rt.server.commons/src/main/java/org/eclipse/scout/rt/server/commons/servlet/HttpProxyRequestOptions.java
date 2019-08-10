/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
