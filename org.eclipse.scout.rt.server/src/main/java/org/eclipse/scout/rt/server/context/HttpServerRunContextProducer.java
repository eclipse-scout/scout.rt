/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.server.commons.HttpSessionMutex;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.shared.session.Sessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link ServerRunContext} based on a {@link HttpServletRequest} and the current JAAS context.
 * <p>
 *   FIXME PBZ SESSION javadoc in class
 * <p>
 */
@Bean
public class HttpServerRunContextProducer {

  public static final String SCOUT_SESSION_ID_KEY = HttpServerRunContextProducer.class.getName() + ".SCOUT_SESSION_ID";
  private static final Logger LOG = LoggerFactory.getLogger(HttpServerRunContextProducer.class);

  private final HttpRunContextProducer m_innerRunContextProducer;
  private boolean m_sessionSupport;

  public HttpServerRunContextProducer() {
    m_innerRunContextProducer = createRunContextProducer();
    m_sessionSupport = true;
  }

  /**
   * @return A new {@link ServerRunContext} based on the {@link HttpServletRequest} specified.<br>
   * If this producer is configured to create sessions, an {@link IServerSession} is created (if not already
   * present) using a new random session id and will be available on the {@link ServerRunContext}.
   */
  public ServerRunContext produce(HttpServletRequest req, HttpServletResponse resp) {
    return produce(req, resp, null, null);
  }

  /**
   * @param req
   * @param resp
   * @param scoutSessionId
   *     The Scout session Id to use or {@code null} to create a new random Id.
   * @param existingContext
   *     The existing {@link ServerRunContext} that should be filled with the values from the
   *     {@link HttpServletRequest} specified. If {@code null}, a new context is created.
   * @return A new {@link ServerRunContext} based on the {@link HttpServletRequest} specified.<br>
   * If this producer is configured to create sessions, an {@link IServerSession} is created (if not already
   * present) using the session id specified and will be available on the {@link ServerRunContext}. The created
   * {@link IServerSession} is bound to the {@link HttpSession} and will be stopped and removed when the
   * {@link HttpSession} is invalidated.
   */
  public ServerRunContext produce(HttpServletRequest req, HttpServletResponse resp, String scoutSessionId, ServerRunContext existingContext) {
    ServerRunContext contextToFill = existingContext;
    if (contextToFill == null) {
      contextToFill = ServerRunContexts.copyCurrent(true);
    }

    String clientSessionId = req.getHeader(SessionId.HTTP_HEADER_NAME);

    final ServerRunContext serverRunContext = (ServerRunContext) getInnerRunContextProducer().produce(req, resp, contextToFill);
    serverRunContext.withUserAgent(HttpClientInfo.get(req).toUserAgents().build());
    serverRunContext.withThreadLocal(SessionId.CURRENT, clientSessionId);
    if (!hasSessionSupport()) {
      // don't touch the session
      return serverRunContext;
    }

    // FIXME PBZ SESSION cleanup both classes
    throw new IllegalStateException("no session support not supported!");
//    final IServerSession session = getOrCreateScoutSession(req, serverRunContext, scoutSessionId);
//    return serverRunContext
//        .withSession(session);
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
   * handle any sessions.
   */
  public HttpRunContextProducer getInnerRunContextProducer() {
    return m_innerRunContextProducer;
  }
}
