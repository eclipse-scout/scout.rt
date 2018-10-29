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
package org.eclipse.scout.rt.server.context;

import java.io.IOException;
import java.security.AccessController;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerConfigProperties.ServerSessionCacheExpirationProperty;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

/**
 * Filter which creates a {@link ServerRunContext} using the current {@link Subject} and calls the next filter inside
 * it. This ensures a proper {@link ServerRunContext} for the subsequent filters and servlet.
 * <p>
 * <b>Important: </b>If no session is associated with the current subject yet, it is obtained by
 * {@link ServerSessionProviderWithCache}. This means the session is cached by userId (see
 * {@link IAccessControlService#getUserId(Subject)}) and only removed from the cache if the TTL expires (see
 * {@link ServerSessionCacheExpirationProperty})! The {@link IServerSession} is not bound to the HTTP session and
 * therefore survives the HTTP session timeouts!
 *
 * @since 6.1
 */
public class ServerRunContextFilter implements Filter {

  private ServerRunContextProducer m_sessionContextProducer;
  private HttpServerRunContextProducer m_httpServerRunContextProducer;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_sessionContextProducer = createSessionRunContextProducer();
    m_httpServerRunContextProducer = createHttpServerRunContextProducer();
  }

  protected HttpServerRunContextProducer createHttpServerRunContextProducer() {
    return BEANS.get(HttpServerRunContextProducer.class)
        .withSessionSupport(false); // session is provided by #getSessionContextProducer()
  }

  protected HttpServerRunContextProducer getHttpServerRunContextProducer() {
    return m_httpServerRunContextProducer;
  }

  protected ServerRunContextProducer createSessionRunContextProducer() {
    // this producer uses ServerSessionProviderWithCache which is a TTL based cache. Not bound to the HTTP session!
    return BEANS.get(ServerRunContextProducer.class);
  }

  protected ServerRunContextProducer getSessionContextProducer() {
    return m_sessionContextProducer;
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;
    lookupRunContext(req, resp).run(() -> chain.doFilter(request, response));
  }

  protected ServerRunContext lookupRunContext(HttpServletRequest req, HttpServletResponse resp) {
    final ServerRunContext sessionContext = getSessionContextProducer().produce(Subject.getSubject(AccessController.getContext()));
    return getHttpServerRunContextProducer().produce(req, resp, null, sessionContext);
  }

  @Override
  public void destroy() {
    m_sessionContextProducer = null;
    m_httpServerRunContextProducer = null;
  }
}
