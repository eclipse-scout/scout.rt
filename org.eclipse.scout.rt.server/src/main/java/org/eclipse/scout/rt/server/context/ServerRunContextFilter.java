/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import java.io.IOException;
import java.security.AccessController;

import javax.security.auth.Subject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerConfigProperties.ServerSessionCacheExpirationProperty;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

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
