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

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Context to modify {@link HttpProxyRequestOptions} with an {@link IHttpProxyRequestOptionsModifier}.
 */
@Bean
public class HttpProxyRequestContext {

  private HttpServletRequest m_request;
  private String m_localContextPathPrefix;

  public HttpServletRequest getRequest() {
    return m_request;
  }

  public HttpProxyRequestContext withRequest(HttpServletRequest request) {
    m_request = request;
    return this;
  }

  public String getLocalContextPathPrefix() {
    return m_localContextPathPrefix;
  }

  public HttpProxyRequestContext withLocalContextPathPrefix(String localContextPathPrefix) {
    m_localContextPathPrefix = localContextPathPrefix;
    return this;
  }
}
