/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;

/**
 *
 */
public class ClientNotficationAddress implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Set<String> m_sessionIds;
  private final Set<String> m_userIds;
  private final String m_excludeNodeId;
  private final boolean m_notifyAllSessions;
  private final boolean m_notifyAllNodes;

  public ClientNotficationAddress(Set<String> sessionIds, Set<String> userIds, boolean notifyAllSessions, boolean notifyAllNodes, String excludeNodeId) {
    m_sessionIds = Collections.unmodifiableSet(CollectionUtility.hashSet(sessionIds));
    m_userIds = Collections.unmodifiableSet(CollectionUtility.hashSet(userIds));
    m_notifyAllSessions = notifyAllSessions;
    m_notifyAllNodes = notifyAllNodes;
    m_excludeNodeId = excludeNodeId;
  }

  public static ClientNotficationAddress createSessionAddress(Set<String> sessionIds) {
    return createSessionAddress(sessionIds, null);
  }

  public static ClientNotficationAddress createSessionAddress(Set<String> sessionIds, String excludeNodeId) {
    return new ClientNotficationAddress(sessionIds, null, false, false, excludeNodeId);
  }

  public static ClientNotficationAddress createUserAddress(Set<String> userIds) {
    return createUserAddress(userIds, null);
  }

  public static ClientNotficationAddress createUserAddress(Set<String> userIds, String excludeNodeId) {
    return new ClientNotficationAddress(null, userIds, false, false, excludeNodeId);
  }

  public static ClientNotficationAddress createAllSessionsAddress() {
    return createAllSessionsAddress(null);
  }

  public static ClientNotficationAddress createAllSessionsAddress(String excludeNodeId) {
    return new ClientNotficationAddress(null, null, true, false, excludeNodeId);
  }

  public static ClientNotficationAddress createAllNodesAddress() {
    return createAllNodesAddress(null);
  }

  public static ClientNotficationAddress createAllNodesAddress(String excludeNodeId) {
    return new ClientNotficationAddress(null, null, false, true, excludeNodeId);
  }

  public Set<String> getSessionIds() {
    return m_sessionIds;
  }

  public Set<String> getUserIds() {
    return m_userIds;
  }

  public boolean isNotifyAllSessions() {
    return m_notifyAllSessions;
  }

  public boolean isNotifyAllNodes() {
    return m_notifyAllNodes;
  }

  public String getExcludeNodeId() {
    return m_excludeNodeId;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("sessions=").append(getSessionIds()).append(", ");
    builder.append("userIds=").append(getUserIds()).append(", ");
    builder.append("excludeNodeId=").append(getExcludeNodeId()).append(", ");
    builder.append("notifyAllSessions=").append(isNotifyAllSessions()).append(", ");
    builder.append("notifyAllNodes=").append(isNotifyAllNodes());
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (m_notifyAllSessions) {
      result = prime * result + (m_notifyAllSessions ? 1431 : 1437);
    }
    else if (m_notifyAllNodes) {
      result = prime * result + (m_notifyAllNodes ? 1231 : 1237);
    }
    else {
      result = prime * result + ((m_excludeNodeId == null) ? 0 : m_excludeNodeId.hashCode());
      result = prime * result + ((m_sessionIds == null) ? 0 : m_sessionIds.hashCode());
      result = prime * result + ((m_userIds == null) ? 0 : m_userIds.hashCode());
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ClientNotficationAddress)) {
      return false;
    }
    ClientNotficationAddress other = (ClientNotficationAddress) obj;
    if (m_notifyAllSessions) {
      return other.m_notifyAllSessions;
    }
    else if (m_notifyAllNodes) {
      return other.m_notifyAllNodes;
    }
    else {
      if (m_excludeNodeId == null) {
        if (other.m_excludeNodeId != null) {
          return false;
        }
      }
      else if (!m_excludeNodeId.equals(other.m_excludeNodeId)) {
        return false;
      }
      if (m_sessionIds == null) {
        if (other.m_sessionIds != null) {
          return false;
        }
      }
      else if (!m_sessionIds.equals(other.m_sessionIds)) {
        return false;
      }
      if (m_userIds == null) {
        if (other.m_userIds != null) {
          return false;
        }
      }
      else if (!m_userIds.equals(other.m_userIds)) {
        return false;
      }
      return true;
    }
  }

}
