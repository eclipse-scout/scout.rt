/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.session;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.server.IServerSession;

/**
 * Cache Entry for {@link IServerSession} and meta data: HttpSessions using this {@link IServerSession} and lifecycle
 * handler.
 */
public class ServerSessionEntry {

  private final List<String> m_httpSessionList = new ArrayList<>(1);
  private final IServerSessionLifecycleHandler m_sessionLifecycleHandler;

  private IServerSession m_serverSession;

  public ServerSessionEntry(IServerSessionLifecycleHandler sessionLifecycleHandler) {
    m_sessionLifecycleHandler = sessionLifecycleHandler;
  }

  public IServerSession getOrCreateScoutSession() {
    if (m_serverSession == null) {
      m_serverSession = m_sessionLifecycleHandler.create();
    }
    return m_serverSession;
  }

  public void addHttpSessionId(String httpSessionId) {
    m_httpSessionList.add(httpSessionId);
  }

  public void removeHttpSession(String httpSessionId) {
    m_httpSessionList.remove(httpSessionId);
  }

  public boolean hasNoMoreHttpSessions() {
    return m_httpSessionList.isEmpty();
  }

  public void destroy() {
    if (m_serverSession != null) {
      m_sessionLifecycleHandler.destroy(m_serverSession);
    }
  }

}
