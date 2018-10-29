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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;

/**
 * Cache Entry for {@link IServerSession} and meta data: HttpSessions using this {@link IServerSession} and lifecycle
 * handler.
 */
public class ServerSessionEntry {

  // must be a set. in case multiple parallel requests from the same client arrive at the same time. then the same HTTP session id may be added several times. But it is only removed once, when the session is invalidated by the container.
  private final Set<String> m_httpSessionList = new HashSet<>(1);
  private final IServerSessionLifecycleHandler m_sessionLifecycleHandler;
  private final FinalValue<IServerSession> m_serverSession;

  protected ServerSessionEntry(IServerSessionLifecycleHandler sessionLifecycleHandler) {
    m_serverSession = new FinalValue<>();
    m_sessionLifecycleHandler = sessionLifecycleHandler;
  }

  protected IServerSession getOrCreateScoutSession() {
    return m_serverSession.setIfAbsentAndGet(m_sessionLifecycleHandler::create);
  }

  /**
   * @return The {@link IServerSession} of this entry or {@code null} if no session has been created yet.
   */
  public IServerSession getScoutSession() {
    return m_serverSession.get();
  }

  protected IServerSessionLifecycleHandler getServerSessionLifecycleHandler() {
    return m_sessionLifecycleHandler;
  }

  protected void addHttpSessionId(String httpSessionId) {
    m_httpSessionList.add(httpSessionId);
  }

  protected void removeHttpSessionId(String httpSessionId) {
    m_httpSessionList.remove(httpSessionId);
  }

  /**
   * @return the number of HTTP sessions that use the {@link IServerSession} of this entry.
   */
  public int httpSessionCount() {
    return m_httpSessionList.size();
  }

  protected void destroy() {
    IServerSession session = getScoutSession();
    if (session == null) {
      return;
    }

    if (ServerSessionProvider.currentSession() == session) {
      m_sessionLifecycleHandler.destroy(session);
    }
    else {
      // in case the destroy is called by the servlet container when invalidating the HTTP session: ensure the session is on the context
      ServerRunContexts
          .copyCurrent(true)
          .withSession(session)
          .run(() -> m_sessionLifecycleHandler.destroy(session));
    }
  }
}
