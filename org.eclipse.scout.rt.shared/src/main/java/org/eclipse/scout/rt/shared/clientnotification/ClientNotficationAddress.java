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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.ToStringBuilder;

public class ClientNotficationAddress implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Set<String> m_sessionIds = new HashSet<>();
  private final Set<String> m_userIds = new HashSet<>();
  private String m_excludedNodeId = null;
  private boolean m_notifyAllSessions = false;
  private boolean m_notifyAllNodes = false;

  /**
   * Only one of the following expression must evaluate to true (listed in order of evaluation in
   * ClientNotificationDispatcher):
   * <ul>
   * <li>notifyAllNodes</li>
   * <li>notifyAllSessions</li>
   * <li>!CollectionUtility.isEmpty(sessionIds)</li>
   * <li>!CollectionUtility.isEmpty(userIds)</li>
   * </ul>
   **/
  protected ClientNotficationAddress() {
  }

  public static ClientNotficationAddress createSessionAddress(final Set<String> sessionIds) {
    return new ClientNotficationAddress().withSessionIds(sessionIds);
  }

  public static ClientNotficationAddress createSessionAddress(final Set<String> sessionIds, final String excludedNodeId) {
    return new ClientNotficationAddress().withSessionIds(sessionIds).withExcludedNodeId(excludedNodeId);
  }

  public static ClientNotficationAddress createUserAddress(final Set<String> userIds) {
    return new ClientNotficationAddress().withUserIds(userIds);
  }

  public static ClientNotficationAddress createUserAddress(final Set<String> userIds, final String excludedNodeId) {
    return new ClientNotficationAddress().withUserIds(userIds).withExcludedNodeId(excludedNodeId);
  }

  public static ClientNotficationAddress createAllSessionsAddress() {
    return new ClientNotficationAddress().withNotifyAllSessions(true);
  }

  public static ClientNotficationAddress createAllSessionsAddress(final String excludedNodeId) {
    return new ClientNotficationAddress().withNotifyAllSessions(true).withExcludedNodeId(excludedNodeId);
  }

  public static ClientNotficationAddress createAllNodesAddress() {
    return new ClientNotficationAddress().withNotifyAllNodes(true);
  }

  public static ClientNotficationAddress createAllNodesAddress(final String excludedNodeId) {
    return new ClientNotficationAddress().withNotifyAllNodes(true).withExcludedNodeId(excludedNodeId);
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

  public String getExcludedNodeId() {
    return m_excludedNodeId;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("sessions", getSessionIds());
    builder.attr("userIds", getUserIds());
    builder.attr("excludedNodeId", getExcludedNodeId());
    builder.attr("notifyAllSessions", isNotifyAllSessions());
    builder.attr("notifyAllNodes", isNotifyAllNodes());
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
      result = prime * result + ((m_excludedNodeId == null) ? 0 : m_excludedNodeId.hashCode());
      result = prime * result + ((m_sessionIds == null) ? 0 : m_sessionIds.hashCode());
      result = prime * result + ((m_userIds == null) ? 0 : m_userIds.hashCode());
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ClientNotficationAddress)) {
      return false;
    }
    final ClientNotficationAddress other = (ClientNotficationAddress) obj;
    if (m_notifyAllSessions) {
      return other.m_notifyAllSessions;
    }
    else if (m_notifyAllNodes) {
      return other.m_notifyAllNodes;
    }
    else {
      if (m_excludedNodeId == null) {
        if (other.m_excludedNodeId != null) {
          return false;
        }
      }
      else if (!m_excludedNodeId.equals(other.m_excludedNodeId)) {
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

  protected ClientNotficationAddress withSessionIds(final Set<String> sessionIds) {
    m_sessionIds.addAll(sessionIds);
    return this;
  }

  protected ClientNotficationAddress withUserIds(final Set<String> userIds) {
    m_userIds.addAll(userIds);
    return this;
  }

  protected ClientNotficationAddress withNotifyAllSessions(final boolean notifyAllSessions) {
    m_notifyAllSessions = notifyAllSessions;
    return this;
  }

  protected ClientNotficationAddress withNotifyAllNodes(final boolean notifyAllNodes) {
    m_notifyAllNodes = notifyAllNodes;
    return this;
  }

  protected ClientNotficationAddress withExcludedNodeId(final String excludedNodeId) {
    m_excludedNodeId = excludedNodeId;
    return this;
  }
}
