/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.concurrent.GroupedSynchronizer;
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

  private final GroupedSynchronizer<String, ServerSessionEntry> m_lockBySessionId = new GroupedSynchronizer<>(true);

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
    return m_lockBySessionId.applyInGroupLock(sessionLifecycleHandler.getId(),
        sessionContext -> getOrCreate(sessionContext, httpSession),
        sessionId -> new ServerSessionEntry(sessionLifecycleHandler));
  }

  protected IServerSession getOrCreate(final ServerSessionEntry sessionContext, HttpSession httpSession) {
    final IServerSession session = sessionContext.getOrCreateScoutSession();
    if (session == null) {
      // do not remove the entry here. Possibility for deadlock (see GroupedSynchronizer#remove).
      // it makes no sense to use a ServerSessionCache if there is no session available anyway.
      LOG.warn("No class implementing {} could be found. If no server session class is available, using a {} is not necessary. Please fix your configuration to skip the {} creation.",
          IServerSession.class.getName(), ServerSessionCache.class.getName(), IServerSession.class.getName());
      return null;
    }

    boolean newlyAdded = sessionContext.addHttpSessionId(httpSession.getId());
    if (newlyAdded) {
      // only set the attributes if it is not already set
      // otherwise this might trigger an unbound event on the old SessionBindingListener which leads to a deadlock
      httpSession.setAttribute(SERVER_SESSION_KEY, session);
      httpSession.setAttribute(UNBIND_LISTENER_KEY, new ScoutSessionBindingListener(session.getId()));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scout ServerSession Session added to HttpSession [scoutSessionId={}, httpSessionId={}]", session.getId(), httpSession.getId());
      }
    }
    return session;
  }

  /**
   * Remove HTTP session and destroy the scout session, if no more {@link HttpSession}s for this scout session are
   * available.
   *
   * @param scoutSessionId
   *          is the groupId referencing multiple http sessions
   * @param httpSessionId
   *          must not be {@code null}.
   */
  public void removeHttpSession(String scoutSessionId, String httpSessionId) {
    final ServerSessionEntry removedEntry = m_lockBySessionId.remove(scoutSessionId, entry -> removeEntry(entry, httpSessionId));
    if (removedEntry != null) {
      LOG.debug("Removed Scout server session from cache [scoutSessionId={}, httpSessionId={}].", scoutSessionId, httpSessionId);
      // destroy entry that was removed from the cache.
      // execute it outside the lock so that new sessions may be created again while the old one is still stopping.
      // no try necessary. Throw any exceptions while stopping up to the caller. The caches are in sync already anyway.
      removedEntry.destroy();
    }
  }

  protected boolean removeEntry(ServerSessionEntry scoutSessionContext, String httpSessionId) {
    scoutSessionContext.removeHttpSessionId(httpSessionId);
    return scoutSessionContext.httpSessionCount() < 1;
  }

  /**
   * Read-only view on the internal cache map. Key is the Scout session id, value the associated cached entry.
   */
  public Map<String, ServerSessionEntry> cacheMap() {
    return m_lockBySessionId.toMap();
  }

  public int numRootLocks() {
    return m_lockBySessionId.numRootLocks();
  }

  public int numLockedRootLocks() {
    return m_lockBySessionId.numLockedRootLocks();
  }

  /**
   * @return number of cached items.
   */
  public int size() {
    return m_lockBySessionId.size();
  }
}
