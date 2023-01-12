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

import java.beans.ConstructorProperties;

import javax.management.MXBean;

@MXBean
public interface IServerSessionCacheMBean {

  int getCacheSize();

  int getNumRootLocks();

  int getNumLockedRootLocks();

  ServerSessionCacheEntry[] getEntries();

  class ServerSessionCacheEntry {

    private final String m_scoutSessionId;
    private final int m_numHttpSessions;
    private final String m_scoutSessionStatus;

    @ConstructorProperties({"scoutSessionId", "numHttpSessions", "scoutSessionStatus"})
    public ServerSessionCacheEntry(String scoutSessionId, int numHttpSessions, String scoutSessionStatus) {
      m_scoutSessionId = scoutSessionId;
      m_numHttpSessions = numHttpSessions;
      m_scoutSessionStatus = scoutSessionStatus;
    }

    public String getScoutSessionId() {
      return m_scoutSessionId;
    }

    public int getNumHttpSessions() {
      return m_numHttpSessions;
    }

    public String getScoutSessionStatus() {
      return m_scoutSessionStatus;
    }
  }
}
