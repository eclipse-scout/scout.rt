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
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Determines how the client notification needs to be dispatched and handled. A client notification can be addressed to
 * <ul>
 * <li>all nodes</li>
 * <li>all sessions</li>
 * <li>one or more specific sessions</li>
 * <li>one or more specific users</li>
 * </ul>
 */
public class ClientNotificationAddress implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Set<String> m_sessionIds = new HashSet<>();
  private final Set<String> m_userIds = new HashSet<>();
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
  protected ClientNotificationAddress() {
  }

  /**
   * Create an address for a notification that should to be handled once for each of the given sessions.
   *
   * @param sessionIds
   *          the ids of the users for this address see <code>ISession.getId()</code>
   * @return notification address
   */
  public static ClientNotificationAddress createSessionAddress(final Set<String> sessionIds) {
    return new ClientNotificationAddress().withSessionIds(sessionIds);
  }

  /**
   * Create an address for a notification that should to be handled once for each of the given users.
   *
   * @param userIds
   *          the ids of the users for this address see <code>ISession.getUserId()</code>
   * @return notification address
   */
  public static ClientNotificationAddress createUserAddress(final Set<String> userIds) {
    return new ClientNotificationAddress().withUserIds(userIds);
  }

  /**
   * Create an address for a notification that should to be handled once for each session.
   *
   * @return notification address
   */
  public static ClientNotificationAddress createAllSessionsAddress() {
    return new ClientNotificationAddress().withNotifyAllSessions(true);
  }

  /**
   * Create an address for a notification that should to be handled once per client node.
   *
   * @return notification address
   */
  public static ClientNotificationAddress createAllNodesAddress() {
    return new ClientNotificationAddress().withNotifyAllNodes(true);
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

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("sessions", getSessionIds());
    builder.attr("userIds", getUserIds());
    builder.attr("notifyAllSessions", isNotifyAllSessions());
    builder.attr("notifyAllNodes", isNotifyAllNodes());
    return builder.toString();
  }

  @Override
  @SuppressWarnings("squid:S2583")
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
      result = prime * result + ((m_sessionIds == null) ? 0 : m_sessionIds.hashCode());
      result = prime * result + ((m_userIds == null) ? 0 : m_userIds.hashCode());
    }
    return result;
  }

  @Override
  @SuppressWarnings("squid:S2583")
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ClientNotificationAddress)) {
      return false;
    }
    final ClientNotificationAddress other = (ClientNotificationAddress) obj;
    if (m_notifyAllSessions) {
      return other.m_notifyAllSessions;
    }
    else if (m_notifyAllNodes) {
      return other.m_notifyAllNodes;
    }
    else {
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

  protected ClientNotificationAddress withSessionIds(final Set<String> sessionIds) {
    m_sessionIds.addAll(sessionIds);
    return this;
  }

  protected ClientNotificationAddress withUserIds(final Set<String> userIds) {
    m_userIds.addAll(userIds);
    return this;
  }

  protected ClientNotificationAddress withNotifyAllSessions(final boolean notifyAllSessions) {
    m_notifyAllSessions = notifyAllSessions;
    return this;
  }

  protected ClientNotificationAddress withNotifyAllNodes(final boolean notifyAllNodes) {
    m_notifyAllNodes = notifyAllNodes;
    return this;
  }
}
