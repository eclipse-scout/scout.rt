/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An {@link IHttpResponseInterceptor} that adds a HTTP header from the given key/value pair to the response.
 */
public class HttpResponseHeaderContributor implements IHttpResponseInterceptor {
  private static final long serialVersionUID = 1L;

  private final String m_name;
  private final String m_value;

  public HttpResponseHeaderContributor(String name, String value) {
    m_name = name;
    m_value = value;
  }

  public String getName() {
    return m_name;
  }

  public String getValue() {
    return m_value;
  }

  @Override
  public void intercept(HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader(m_name, m_value);
  }
}
