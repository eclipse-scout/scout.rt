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

import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.LRUCache;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;

/**
 * Provider for server sessions. A server session is only created if not contained in the session cache.
 */
@ApplicationScoped
public class ServerSessionProviderWithCache extends ServerSessionProvider {

  private static final String PROP_SESSION_EXPIRATION = String.format("%s#expiration", ServerSessionProviderWithCache.class.getName());

  private final LRUCache<CompositeObject, IServerSession> m_cache;

  public ServerSessionProviderWithCache() {
    m_cache = new LRUCache<>(1000, ConfigIniUtility.getPropertyLong(PROP_SESSION_EXPIRATION, TimeUnit.DAYS.toMillis(1)));
  }

  @Override
  public <SESSION extends IServerSession> SESSION provide(final ServerJobInput input) throws ProcessingException {
    final Subject subject = Assertions.assertNotNull(input.getSubject(), "Subject must not be null");
    final Set<Principal> principals = subject.getPrincipals();
    Assertions.assertFalse(principals.isEmpty(), "Subject contains no principals");

    SESSION serverSession = getFromCache(principals, OBJ.one(IServerSession.class).getClass());
    if (serverSession != null) {
      return serverSession;
    }
    else {
      // create and initialize a new session; use optimistic locking because initializing the session is a long-running operation.
      final SESSION newServerSession = super.provide(input);

      synchronized (m_cache) {
        serverSession = getFromCache(principals, newServerSession.getClass()); // optimistic locking: check, whether another thread already created and cached the session.
        if (serverSession != null) {
          return serverSession;
        }
        else {
          return putToCache(principals, newServerSession);
        }
      }
    }
  }

  @Internal
  protected <SESSION extends IServerSession> SESSION getFromCache(final Collection<Principal> principals, final Class<? extends IServerSession> serverSessionClass) throws ProcessingException {
    for (final Principal principal : principals) {
      final IServerSession serverSession = m_cache.get(newCacheKey(serverSessionClass, principal));
      if (serverSession != null) {
        return ServerSessionProvider.cast(serverSession);
      }
    }
    return null;
  }

  @Internal
  protected <SESSION extends IServerSession> SESSION putToCache(final Collection<Principal> principals, final SESSION serverSession) throws ProcessingException {
    for (final Principal principal : principals) {
      m_cache.put(newCacheKey(serverSession.getClass(), principal), serverSession);
    }
    return serverSession;
  }

  @Internal
  protected CompositeObject newCacheKey(final Class<? extends IServerSession> serverSessionClass, final Principal principal) {
    return new CompositeObject(serverSessionClass, principal);
  }
}
