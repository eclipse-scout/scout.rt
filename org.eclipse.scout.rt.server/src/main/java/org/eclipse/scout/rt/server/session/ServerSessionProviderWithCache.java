/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.session;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerConfigProperties.ServerSessionCacheExpirationProperty;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

/**
 * Provider for server sessions. A server session is only created if not contained in the session cache.
 */
public class ServerSessionProviderWithCache extends ServerSessionProvider {

  private final ConcurrentMap<CompositeObject, IServerSession> m_cache;

  public ServerSessionProviderWithCache() {
    m_cache = createCacheMap();
  }

  protected ConcurrentMap<CompositeObject, IServerSession> createCacheMap() {
    long ttl = NumberUtility.nvl(CONFIG.getPropertyValue(ServerSessionCacheExpirationProperty.class), 0L);
    return new ConcurrentExpiringMap<CompositeObject, IServerSession>(ttl, TimeUnit.MILLISECONDS, 1000);
  }

  @Override
  public <SESSION extends IServerSession> SESSION provide(ServerRunContext runContext, String sessionId) {
    CompositeObject cacheKey = newCacheKey(runContext, sessionId);
    @SuppressWarnings("unchecked")
    SESSION serverSession = (SESSION) m_cache.get(cacheKey);
    if (serverSession != null) {
      return serverSession;
    }
    else {
      // create and initialize a new session; use optimistic locking because initializing the session is a long-running operation.
      serverSession = super.provide(runContext, sessionId);

      @SuppressWarnings("unchecked")
      SESSION oldServerSession = (SESSION) m_cache.putIfAbsent(cacheKey, serverSession);
      if (oldServerSession != null) {
        // optimistic locking: check, whether another thread already created and cached the session.
        return oldServerSession;
      }
      return serverSession;
    }
  }

  protected CompositeObject newCacheKey(ServerRunContext runContext, String sessionId) {
    Subject subject = Assertions.assertNotNull(runContext.getSubject(), "Subject must not be null");
    Class<? extends IServerSession> serverSessionClass = BEANS.get(IServerSession.class).getClass();
    String userId = BEANS.get(IAccessControlService.class).getUserId(subject);
    // if userId can not be determined, use sessionId as key and force therefore to create
    // and return a new session.
    return new CompositeObject(serverSessionClass, StringUtility.nvl(userId, sessionId));
  }
}
