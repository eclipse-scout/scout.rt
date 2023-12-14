/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxy;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestContext;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;

public abstract class AbstractRestProxyRequestHandler extends AbstractUiServletRequestHandler {

  private HttpProxy m_proxy;

  public AbstractRestProxyRequestHandler() {
    setProxyInternal(BEANS.get(HttpProxy.class));
  }

  protected void setProxyInternal(HttpProxy proxy) {
    m_proxy = proxy;
  }

  protected HttpProxy getProxy() {
    return m_proxy;
  }

  /**
   * Override this method to initialize the {@link HttpProxy} instance.
   */
  @PostConstruct
  protected void initialize() {
    getProxy().withRemoteBaseUrl(getRemoteBaseUrl());
  }

  /**
   * @return the base URL of the remote server that is used when rewriting URLs.
   */
  protected abstract String getRemoteBaseUrl();

  /**
   * @return the local context path prefix this request handler listens to. Example: <code>"/api/"</code>
   */
  protected abstract String getLocalContextPathPrefix();

  protected boolean acceptRequest(HttpServletRequest req) {
    return StringUtility.startsWith(req.getPathInfo(), getLocalContextPathPrefix());
  }

  @Override
  public boolean handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!acceptRequest(req)) {
      return false;
    }
    proxy(req, resp);
    return true;
  }

  protected void proxy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HttpProxyRequestOptions options = createHttpProxyRequestOptions(req, resp);
    getProxy().proxy(req, resp, options);
  }

  /**
   * @return options to be used for an HTTP through the proxy
   */
  protected abstract HttpProxyRequestOptions createHttpProxyRequestOptions(HttpServletRequest req, HttpServletResponse resp);

  protected HttpProxyRequestContext createHttpProxyRequestContext(HttpServletRequest req) {
    return BEANS.get(HttpProxyRequestContext.class)
        .withRequest(req)
        .withLocalContextPathPrefix(getLocalContextPathPrefix());
  }
}
