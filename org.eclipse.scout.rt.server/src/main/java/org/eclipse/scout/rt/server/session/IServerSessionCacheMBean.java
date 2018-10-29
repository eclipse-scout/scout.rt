/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.session;

import java.beans.ConstructorProperties;

import javax.management.MXBean;

@MXBean
public interface IServerSessionCacheMBean {

  int getCacheSize();

  int getNumRootLocks();

  int getNumLockedRootLocks();

  ServerSessionCacheEntry[] getEntries();

  public class ServerSessionCacheEntry {

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
