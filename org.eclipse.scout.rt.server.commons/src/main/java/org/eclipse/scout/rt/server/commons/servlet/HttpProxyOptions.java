/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.HashMap;
import java.util.Map;

/**
 * <ul>
 * <li><b>rewriteRule</b> A regular expression string that is applied on the <i>pathInfo</i> part of the current HTTP
 * request</li>
 * <li><b>rewriteReplacement</b> Used as replacement string for the string matched by the rewrite rule</li>
 * </ul>
 */
public class HttpProxyOptions {

  private final Map<String, String> m_customRequestHeaders;

  private String m_rewriteRule;

  private String m_rewriteReplacement;

  public HttpProxyOptions() {
    m_customRequestHeaders = new HashMap<>();
  }

  public HttpProxyOptions withCustomRequestHeader(String name, String value) {
    m_customRequestHeaders.put(name, value);
    return this;
  }

  public Map<String, String> getCustomRequestHeaders() {
    return m_customRequestHeaders;
  }

  public HttpProxyOptions withRewriteRule(String rewriteRule) {
    m_rewriteRule = rewriteRule;
    return this;
  }

  public String getRewriteRule() {
    return m_rewriteRule;
  }

  public HttpProxyOptions withRewriteReplacement(String rewriteReplacement) {
    m_rewriteReplacement = rewriteReplacement;
    return this;
  }

  public String getRewriteReplacement() {
    return m_rewriteReplacement;
  }

}
