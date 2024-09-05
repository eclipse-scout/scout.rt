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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerConfigProperties.ServerSessionCacheExpirationProperty;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central point to obtain cached server sessions.
 * <p>
 * If a session is found in cache, this session is returned, or else a new session created via
 * {@link ServerSessionProvider}.
 * <p>
 * A session is identified by its <em>sessionId</em>, or if not specified its <em>userId</em>.
 *
 * @since 5.1
 */
public class ServerSessionProviderWithCache extends ServerSessionProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ServerSessionProviderWithCache.class);

  private final ConcurrentExpiringMap<CompositeObject, IServerSession> m_cache;

  public ServerSessionProviderWithCache() {
    m_cache = createSessionCache(CONFIG.getPropertyValue(ServerSessionCacheExpirationProperty.class));
  }

  /**
   * Returns the cached server session for the context's {@link Subject}. On cache miss, a new session with a random
   * <em>sessionId</em> is created via {@link ServerSessionProvider}.
   *
   * @param serverRunContext
   *          applied during session start, and to get the session's {@link Subject}.
   * @return session found in cache, or a new session on cache miss.
   * @throws RuntimeException
   *           if session creation failed.
   */
  @Override
  public <SESSION extends IServerSession> SESSION provide(final ServerRunContext serverRunContext) {
    return provide(null, serverRunContext);
  }

  /**
   * Returns the cached server session for the given <em>sessionId</em>, or the context's {@link Subject} if
   * <em>sessionId</em> is not specified. On cache miss, a new session is created via {@link ServerSessionProvider}.
   *
   * @param sessionId
   *          unique session ID to identify the cached session. If <code>null</code>, the context's {@link Subject} is
   *          used for identification. On cache miss, this <em>sessionId</em> is used to create a new session, or a
   *          random UUID if <code>null</code>.
   * @param serverRunContext
   *          applied during session start, and to get the session's {@link Subject}.
   * @return session found in cache, or a new session on cache miss.
   * @throws RuntimeException
   *           if session creation failed.
   */
  @Override
  public <SESSION extends IServerSession> SESSION provide(final String sessionId, final ServerRunContext serverRunContext) {
    // 1. Create session lookup key.
    final CompositeObject sessionCacheKey = newSessionCacheKey(sessionId, serverRunContext.getSubject());
    if (sessionCacheKey == null) {
      LOG.warn("Cannot identify cached server session because the cache key is undefined  [sessionId={}, subject={}]", sessionId, serverRunContext.getSubject());
      return super.provide(sessionId, serverRunContext);
    }

    // 2. Lookup session in the cache.
    @SuppressWarnings("unchecked")
    SESSION serverSession = (SESSION) m_cache.get(sessionCacheKey);
    if (serverSession != null) {
      return serverSession;
    }

    // 3. Cache miss (optimistic locking because session creation might be a long running operation)
    serverSession = super.provide(sessionId, serverRunContext);

    // 4. Cache the new server session, or return present session if created by another thread in the meantime (optimistic locking).
    @SuppressWarnings("unchecked")
    final SESSION cachedServerSession = (SESSION) m_cache.putIfAbsent(sessionCacheKey, serverSession);
    if (cachedServerSession != null) {
      serverSession = cachedServerSession;
    }

    return serverSession;
  }

  /**
   * Removes all entries with the specified {@link IServerSession} from this cache instance.
   *
   * @param session
   *          The server session to remove. Must not be {@code null}.
   */
  public void remove(IServerSession session) {
    remove(assertNotNull(session).getId());
  }

  /**
   * Removes all entries with the specified {@link IServerSession} from this cache instance.
   *
   * @param sessionId
   *          The id of the server session to remove. Must not be {@code null}.
   */
  public void remove(String sessionId) {
    assertNotNull(sessionId);
    m_cache.values().removeIf(sess -> sess.getId().equals(sessionId));
  }

  protected ConcurrentExpiringMap<CompositeObject, IServerSession> createSessionCache(final long ttl) {
    return new ConcurrentExpiringMap<>(ttl, TimeUnit.MILLISECONDS, 1_000) {
      @Override
      protected void execEntryEvicted(CompositeObject key, IServerSession session) {
        if (session == null || !session.isActive() || session.isStopping()) {
          return;
        }

        if (ServerSessionProvider.currentSession() == session) {
          session.stop();
        }
        else {
          ServerRunContexts
              .copyCurrent(true)
              .withSession(session)
              .run(session::stop);
        }
      }
    };
  }

  protected CompositeObject newSessionCacheKey(final String sessionId, final Subject subject) {
    if (sessionId != null) {
      return new CompositeObject(sessionId);
    }
    if (subject != null) {
      return new CompositeObject(BEANS.get(IAccessControlService.class).getUserId(subject));
    }
    return null;
  }
}
