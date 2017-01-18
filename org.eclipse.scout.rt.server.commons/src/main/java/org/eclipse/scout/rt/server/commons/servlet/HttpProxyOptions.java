/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.HashMap;
import java.util.Map;

public class HttpProxyOptions {
  private Map<String, String> m_customRequestHeaders;

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
}
