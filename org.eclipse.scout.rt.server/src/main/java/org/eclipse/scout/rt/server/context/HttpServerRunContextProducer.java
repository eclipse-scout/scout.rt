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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.server.session.IServerSessionLifecycleHandler;
import org.eclipse.scout.rt.server.session.ServerSessionCache;
import org.eclipse.scout.rt.server.session.ServerSessionLifecycleHandler;
import org.eclipse.scout.rt.shared.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link ServerRunContext} based on a {@link HttpServletRequest} and the current JAAS context.
 * <p>
 * The parameter "sessionSupport" (see {@link #withSessionSupport(boolean)}) specifies if an {@link IServerSession}
 * should be created. The default is {@code true}.<br>
 * If {@code true}, the Scout server session will be stored on the HTTP session and will be automatically stopped and
 * removed if the HTTP session is invalidated. This means if session support is enabled a cookie capable HTTP client is
 * required! Furthermore a class implementing {@link IServerSession} must be present on the class-path.
 * <p>
 *
 * @since 9.0
 */
@Bean
public class HttpServerRunContextProducer {

  public static final String SCOUT_SESSION_ID_KEY = HttpServerRunContextProducer.class.getName() + ".SCOUT_SESSION_ID";
  private static final Logger LOG = LoggerFactory.getLogger(HttpServerRunContextProducer.class);

  private final ServerSessionCache m_serverSessionCache;
  private final HttpRunContextProducer m_innerRunContextProducer;
  private boolean m_sessionSupport;

  public HttpServerRunContextProducer() {
    m_serverSessionCache = createServerSessionCache();
    m_innerRunContextProducer = createRunContextProducer();
    m_sessionSupport = true;
  }

  /**
   * @return A new {@link ServerRunContext} based on the {@link HttpServletRequest} specified.<br>
   *         If this producer is configured to create sessions, an {@link IServerSession} is created (if not already
   *         present) using a new random session id and will be available on the {@link ServerRunContext}.
   */
  public ServerRunContext produce(HttpServletRequest req, HttpServletResponse resp) {
    return produce(req, resp, null, null);
  }

  /**
   * @param req
   * @param resp
   * @param scoutSessionId
   *          The Scout session Id to use or {@code null} to create a new random Id.
   * @param existingContext
   *          The existing {@link ServerRunContext} that should be filled with the values from the
   *          {@link HttpServletRequest} specified. If {@code null}, a new context is created.
   * @return A new {@link ServerRunContext} based on the {@link HttpServletRequest} specified.<br>
   *         If this producer is configured to create sessions, an {@link IServerSession} is created (if not already
   *         present) using the session id specified and will be available on the {@link ServerRunContext}. The created
   *         {@link IServerSession} is bound to the {@link HttpSession} and will be stopped and removed when the
   *         {@link HttpSession} is invalidated.
   */
  public ServerRunContext produce(HttpServletRequest req, HttpServletResponse resp, String scoutSessionId, ServerRunContext existingContext) {
    ServerRunContext contextToFill = existingContext;
    if (contextToFill == null) {
      contextToFill = ServerRunContexts.copyCurrent(true);
    }

    final ServerRunContext serverRunContext = (ServerRunContext) getInnerRunContextProducer().produce(req, resp, contextToFill);
    serverRunContext.withUserAgent(HttpClientInfo.get(req).toUserAgents().build());
    if (!hasSessionSupport()) {
      // don't touch the session
      return serverRunContext;
    }

    final IServerSession session = getOrCreateScoutSession(req, serverRunContext, scoutSessionId);
    return serverRunContext
        .withSession(session);
  }

  /**
   * Lookup (or create if not existing) an {@link IServerSession} on the {@link HttpServletRequest} specified. If a new
   * session must be created, a random session id is used.
   *
   * @param serverRunContextForSessionStart
   *          If no session is already available: the new session will be started using this {@link ServerRunContext}.
   *          May not be {@code null}.
   * @return the existing or newly created session or {@code null} if this producer has no session support (see
   *         {@link #withSessionSupport(boolean)}).
   */
  public IServerSession getOrCreateScoutSession(HttpServletRequest req, ServerRunContext serverRunContextForSessionStart) {
    return getOrCreateScoutSession(req, serverRunContextForSessionStart, null);
  }

  /**
   * Lookup (or create if not existing) an {@link IServerSession} on the {@link HttpServletRequest} specified. If a new
   * session must be created, the given id is used.
   *
   * @param serverRunContextForSessionStart
   *          If no session is already available: the new session will be started using this {@link ServerRunContext}.
   *          May not be {@code null}.
   * @return the existing or newly created session or {@code null} if this producer has no session support (see
   *         {@link #withSessionSupport(boolean)}).
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

  protected String ensureScoutSessionId(String scoutSessionId, HttpSession httpSession) {
    if (StringUtility.hasText(scoutSessionId)) {
      return scoutSessionId;
    }
    return computeSessionIdIfAbsent(httpSession);
  }

  protected String computeSessionIdIfAbsent(HttpSession httpSession) {
    synchronized (HttpSessionMutex.of(httpSession)) {
      String scoutSessionId = (String) httpSession.getAttribute(SCOUT_SESSION_ID_KEY);
      if (StringUtility.hasText(scoutSessionId)) {
        return scoutSessionId;
      }

      scoutSessionId = Sessions.randomSessionId();
      httpSession.setAttribute(SCOUT_SESSION_ID_KEY, scoutSessionId);
      return scoutSessionId;
    }
  }

  public HttpServerRunContextProducer withSessionSupport(boolean sessionSupport) {
    m_sessionSupport = sessionSupport;
    return this;
  }

  public boolean hasSessionSupport() {
    return m_sessionSupport;
  }

  protected HttpRunContextProducer createRunContextProducer() {
    return BEANS.get(HttpRunContextProducer.class);
  }

  /**
   * @return The nested {@link HttpRunContextProducer} that is used to fill the common attributes. This producer cannot
   *         handle any sessions.
   */
  public HttpRunContextProducer getInnerRunContextProducer() {
    return m_innerRunContextProducer;
  }

  protected ServerSessionCache createServerSessionCache() {
    return BEANS.get(ServerSessionCache.class);
  }

  public ServerSessionCache getServerSessionCache() {
    return m_serverSessionCache;
  }
}
