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
package org.eclipse.scout.rt.server.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.IServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Scout {@link IServerSession} is cached on the {@link HttpSession} and stopped and removed, when the
 * {@link HttpSession} expires.
 * <p>
 * This cache avoids creating multiple {@link IServerSession}s for the same Scout sessionId, even, if the HTTP sessions
 * are not the same. There may be different HttpSessions for the same sessionId, if the serverSession is expired and
 * there are multiple requests in parallel by the same user (with the same client session). To achieve this, the
 * {@link ServerSessionEntry} consisting of scout session, httpSessionIds using this session, as well as the
 * destructionCallback for the Scout server session are cached in this instance.
 */
@ApplicationScoped
public class ServerSessionCache {

  private static final Logger LOG = LoggerFactory.getLogger(ServerSessionCache.class);

  public static final String SERVER_SESSION_KEY = IServerSession.class.getName();
  public static final String UNBIND_LISTENER_KEY = ScoutSessionBindingListener.class.getName();

  private final ConcurrentMap<String, ServerSessionEntry> m_sessionContexts = new ConcurrentHashMap<>();

  /**
   * Lookup the Scout session on the given {@link HttpSession}. Creates a new scout session using the given
   * {@link IServerSessionLifecycleHandler}, if none exists.
   *
   * @param sessionLifecycleHandler
   *          for creating and destroying scout sessions. Must not be {@code null}.
   * @param httpSession
   *          must not be {@code null}.
   * @return new or existing {@link IServerSession} or {@code null} if no server session could be found on the
   *         class-path.
   */
  public IServerSession getOrCreate(IServerSessionLifecycleHandler sessionLifecycleHandler, HttpSession httpSession) {
    Object scoutSession = httpSession.getAttribute(SERVER_SESSION_KEY);
    if (scoutSession instanceof IServerSession) {
      return (IServerSession) scoutSession;
    }

    // lock by scout sessionId to prevent creation of scout session more than once per scoutSessionId
    ServerSessionEntry sessionContext = getSessionContext(sessionLifecycleHandler.getId(), sessionLifecycleHandler);
    synchronized (sessionContext) {
      final IServerSession session = sessionContext.getOrCreateScoutSession();
      if (session == null) {
        removeHttpSession(sessionContext.getServerSessionLifecycleHandler().getId(), httpSession);
        return null;
      }

      sessionContext.addHttpSessionId(httpSession.getId());
      httpSession.setAttribute(SERVER_SESSION_KEY, session);
      httpSession.setAttribute(UNBIND_LISTENER_KEY, new ScoutSessionBindingListener(session.getId()));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scout ServerSession Session added to HttpSession [scoutSessionId={}, httpSessionId={}]", session.getId(), httpSession.getId());
      }
      return session;
    }
  }

  /**
   * Only one ScoutServerSessionContext object per sessionId must exist
   */
  protected ServerSessionEntry getSessionContext(String sessionId, IServerSessionLifecycleHandler sessionLifecycleHandle) {
    return m_sessionContexts.computeIfAbsent(sessionId, id -> new ServerSessionEntry(sessionLifecycleHandle));
  }

  /**
   * Remove HTTP session and destroy the scout session, if no more {@link HttpSession}s for this scout session are
   * available.
   *
   * @param scoutSessionId
   * @param httpSession
   *          May not be {@code null}.
   */
  public void removeHttpSession(String scoutSessionId, HttpSession httpSession) {
    ServerSessionEntry scoutSessionContext = m_sessionContexts.get(scoutSessionId);
    if (scoutSessionContext == null) {
      LOG.warn("Unknown sessionContext [scoutSessionId={}, httpSessionId={}]", scoutSessionId, httpSession.getId());
      return;
    }

    synchronized (scoutSessionContext) {
      scoutSessionContext.removeHttpSessionId(httpSession.getId());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scout ServerSession removed from HttpSession [scoutSessionId={}, httpSessionId={}]", scoutSessionId, httpSession.getId());
      }

      // destroy scout session, if there is no httpsession with this scout session
      if (scoutSessionContext.httpSessionCount() < 1) {
        try {
          scoutSessionContext.destroy();
        }
        finally {
          m_sessionContexts.remove(scoutSessionId);
          if (LOG.isDebugEnabled()) {
            LOG.debug("Removed Scout server session from cache [scoutSessionId={}, httpSessionId={}].", scoutSessionId, httpSession.getId());
          }
        }
      }
    }
  }

  /**
   * @return number of cached items.
   */
  public int size() {
    return m_sessionContexts.size();
  }
}
