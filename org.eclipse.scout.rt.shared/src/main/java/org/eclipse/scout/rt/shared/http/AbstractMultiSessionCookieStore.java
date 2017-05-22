/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;

/**
 * Abstract super-class for multi session cookie stores.
 */
@Bean
public abstract class AbstractMultiSessionCookieStore<CS> {

  private final ReadWriteLock m_cookieStoresLock = new ReentrantReadWriteLock();

  /**
   * Access to this map synchronized with the ReadWriteLock {@link #m_cookieStoresLock}.
   */
  private final Map<ISession, CS> m_cookieStores;
  private final CS m_defaultCookieStore;

  public AbstractMultiSessionCookieStore() {
    // Use a WeakHashMap to ensure that client sessions are not retained when no longer needed.
    m_cookieStores = new WeakHashMap<ISession, CS>();
    m_defaultCookieStore = createDefaultCookieStore();
  }

  protected abstract CS createDefaultCookieStore();

  protected abstract CS createNewCookieStore();

  protected CS getDelegate() {
    ISession currentSession = ISession.CURRENT.get();
    if (currentSession == null) {
      return m_defaultCookieStore;
    }

    // Check cache with read lock first.
    m_cookieStoresLock.readLock().lock();
    try {
      CS cookieStore = m_cookieStores.get(currentSession);
      if (cookieStore != null) {
        return cookieStore;
      }
    }
    finally {
      m_cookieStoresLock.readLock().unlock();
    }
    // No entry found - get write lock to create it
    m_cookieStoresLock.writeLock().lock();
    try {
      // In the meantime, the cookie store might have been created already by another thread - check again.
      CS cookieStore = m_cookieStores.get(currentSession);
      if (cookieStore != null) {
        return cookieStore;
      }
      else {
        cookieStore = createNewCookieStore();
        m_cookieStores.put(currentSession, cookieStore);
        currentSession.addListener(new P_SessionStoppedListenr());
        return cookieStore;
      }
    }
    finally {
      m_cookieStoresLock.writeLock().unlock();
    }
  }

  public void sessionStopped(ISession session) {
    m_cookieStoresLock.writeLock().lock();
    try {
      m_cookieStores.remove(session);
    }
    finally {
      m_cookieStoresLock.writeLock().unlock();
    }
  }

  private class P_SessionStoppedListenr implements ISessionListener {
    @Override
    public void sessionChanged(SessionEvent event) {
      if (SessionEvent.TYPE_STOPPED == event.getType()) {
        ISession session = event.getSource();
        sessionStopped(session);
        session.removeListener(this);
      }
    }
  }
}
