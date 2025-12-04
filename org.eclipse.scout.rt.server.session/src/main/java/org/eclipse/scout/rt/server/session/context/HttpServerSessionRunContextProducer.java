/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.server.context.HttpServerRunContextProducer;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.server.session.IServerSessionLifecycleHandler;
import org.eclipse.scout.rt.server.session.ServerSessionCache;
import org.eclipse.scout.rt.server.session.ServerSessionLifecycleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Replace
public class HttpServerSessionRunContextProducer extends HttpServerRunContextProducer {

  private static final Logger LOG = LoggerFactory.getLogger(HttpServerSessionRunContextProducer.class);

  private final ServerSessionCache m_serverSessionCache;

  public HttpServerSessionRunContextProducer() {
    m_serverSessionCache = createServerSessionCache();
  }

  @Override
  public ServerSessionRunContext produce(HttpServletRequest req, HttpServletResponse resp) {
    return produce(req, resp, null, null);
  }

  @Override
  public ServerSessionRunContext produce(HttpServletRequest req, HttpServletResponse resp, String scoutSessionId, ServerRunContext existingContext) {
    ServerRunContext contextToFill = existingContext;
    if (contextToFill == null) {
      contextToFill = ServerRunContexts.copyCurrent(true);
    }

    final ServerSessionRunContext serverRunContext = (ServerSessionRunContext) getInnerRunContextProducer().produce(req, resp, contextToFill);
    serverRunContext.withUserAgent(HttpClientInfo.get(req).toUserAgents().build());
    if (!hasSessionSupport()) {
      // don't touch the session
      return serverRunContext;
    }

    final IServerSession session = getOrCreateScoutSession(req, serverRunContext, scoutSessionId);
    return serverRunContext
        .withSession(session);
  }

  protected ServerSessionCache createServerSessionCache() {
    return BEANS.get(ServerSessionCache.class);
  }

  public ServerSessionCache getServerSessionCache() {
    return m_serverSessionCache;
  }

  /**
   * Lookup (or create if not existing) an {@link IServerSession} on the {@link HttpServletRequest} specified. If a new
   * session must be created, a random session id is used.
   *
   * @param serverRunContextForSessionStart
   *     If no session is already available: the new session will be started using this {@link ServerRunContext}.
   *     May not be {@code null}.
   * @return the existing or newly created session or {@code null} if this producer has no session support (see
   * {@link #withSessionSupport(boolean)}).
   */
  public IServerSession getOrCreateScoutSession(HttpServletRequest req, ServerRunContext serverRunContextForSessionStart) {
    return getOrCreateScoutSession(req, serverRunContextForSessionStart, null);
  }

  /**
   * Lookup (or create if not existing) an {@link IServerSession} on the {@link HttpServletRequest} specified. If a new
   * session must be created, the given id is used.
   *
   * @param serverRunContextForSessionStart
   *     If no session is already available: the new session will be started using this {@link ServerRunContext}.
   *     May not be {@code null}.
   * @return the existing or newly created session or {@code null} if this producer has no session support (see
   * {@link #withSessionSupport(boolean)}).
   */
  public IServerSession getOrCreateScoutSession(HttpServletRequest req, ServerRunContext serverRunContextForSessionStart, String scoutSessionId) {
    if (!hasSessionSupport()) {
      return null;
    }

    final HttpSession httpSession = req.getSession();
    final String sid = ensureScoutSessionId(scoutSessionId, httpSession);
    final IServerSessionLifecycleHandler lifecycleHandler = new ServerSessionLifecycleHandler(sid, serverRunContextForSessionStart);
    final IServerSession session = getServerSessionCache().getOrCreate(lifecycleHandler, httpSession);
    if (session == null) {
      LOG.warn("{} is configured to create a Scout session but no class implementing {} could be found. Consider disabling session support.",
          HttpServerRunContextProducer.class.getName(), IServerSession.class.getName());
    }
    return session;
  }
}

