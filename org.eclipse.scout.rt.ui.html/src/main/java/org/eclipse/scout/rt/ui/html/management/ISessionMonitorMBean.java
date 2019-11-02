/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.management;

import java.beans.ConstructorProperties;
import java.util.Date;
import java.util.List;

import javax.management.MXBean;

import org.eclipse.scout.rt.ui.html.ISessionStore;

/**
 * Jmx control collecting statistics of all {@link ISessionStore}
 *
 * @since 9.0
 */
@MXBean
public interface ISessionMonitorMBean {

  int getNumHttpSessions();

  int getNumUiSessions();

  int getNumClientSessions();

  /**
   * @return all session details ordered by uiState, clientState, uiSessionId
   */
  List<SessionDetail> getSessionDetails();

  /**
   * @return all session details ordered by uiState, clientState, uiSessionId
   */
  List<String> getSessionTable();

  class SessionDetail {
    private final String m_httpSessionId;
    private final String m_clientSessionId;
    private final String m_uiSessionId;
    private final String m_userId;
    private final String m_uiState;
    private final String m_clientState;
    private final long m_lastAccessed;

    @ConstructorProperties({"httpSessionId", "uiSessionId", "clientSessionId", "userId", "uiState", "clientState", "lastAccessed"})
    public SessionDetail(String httpSessionId, String uiSessionId, String clientSessionId, String userId, String uiState, String clientState, long lastAccessed) {//NOSONAR
      m_httpSessionId = httpSessionId;
      m_uiSessionId = uiSessionId;
      m_clientSessionId = clientSessionId;
      m_userId = userId;
      m_uiState = uiState;
      m_clientState = clientState;
      m_lastAccessed = lastAccessed;
    }

    public String getHttpSessionId() {
      return m_httpSessionId;
    }

    public String getUiSessionId() {
      return m_uiSessionId;
    }

    public String getClientSessionId() {
      return m_clientSessionId;
    }

    public String getUserId() {
      return m_userId;
    }

    public String getUiState() {
      return m_uiState;
    }

    public String getClientState() {
      return m_clientState;
    }

    public Date getLastAccessed() {
      return new Date(m_lastAccessed);
    }

    public long getAgeInSeconds() {
      return (System.currentTimeMillis() - m_lastAccessed) / 1000L;
    }

    public static String toCsvHeader() {
      return ""
          + "UiState | "
          + "ClientState | "
          + "Age[s] | "
          + "LastAccessed | "
          + "UserId | "
          + "HttpSession | "
          + "UiSession | "
          + "ClientSession";
    }

    public String toCsvRow() {
      return ""
          + m_uiState + " | "
          + m_clientState + " | "
          + getAgeInSeconds() + " | "
          + getLastAccessed() + " | "
          + m_userId + " | "
          + m_httpSessionId + " | "
          + m_uiSessionId + " | "
          + m_clientSessionId;
    }

    @Override
    public String toString() {
      return toCsvRow();
    }
  }
}
