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
package org.eclipse.scout.rt.server.services.common.clientnotification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.rt.server.services.common.notification.INotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 *
 */
public class ClientNotificationNotification implements INotification {

  private static final long serialVersionUID = -8513131031858145786L;
  private IClientNotification m_notification;
  private IClientNotificationFilter m_filter;
  private transient Object m_consumedByClientIdLock;
  private Set<String> m_consumedByClientId;
  private String elementId;

  public ClientNotificationNotification(IClientNotification notification, IClientNotificationFilter filter) {
    m_notification = notification;
    m_filter = filter;
    m_consumedByClientIdLock = new Object();
    elementId = UUID.randomUUID().toString();
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    m_consumedByClientIdLock = new Object();
  }

  public IClientNotification getClientNotification() {
    return m_notification;
  }

  public IClientNotificationFilter getFilter() {
    return m_filter;
  }

  /**
   * @return true if this notifcation is already consumed by the session specified
   */
  public boolean isConsumedBy(String clientId) {
    // fast check
    if (clientId == null) {
      return false;
    }
    if (m_consumedByClientId == null) {
      return false;
    }
    //
    synchronized (m_consumedByClientIdLock) {
      if (m_consumedByClientId != null) {
        return m_consumedByClientId.contains(clientId);
      }
      else {
        return false;
      }
    }
  }

  /**
   * keeps in mind that this notifcation was consumed by the session specified
   */
  public void setConsumedBy(String clientId) {
    if (clientId != null) {
      synchronized (m_consumedByClientIdLock) {
        if (m_consumedByClientId == null) {
          m_consumedByClientId = new HashSet<String>();
        }
        m_consumedByClientId.add(clientId);
      }
    }
  }

  /**
   * @return Map
   */
  public Set<String> getConsumedBy() {
    return m_consumedByClientId;
  }

  /**
   * Adds a Set of SessionIds to the consumed sessionIds
   * 
   * @param consumedByClientIds
   *          Set of SessionIds
   */
  public void addConsumedBy(Set<String> consumedByClientIds) {
    synchronized (m_consumedByClientIdLock) {
      if (m_consumedByClientId == null) {
        m_consumedByClientId = new HashSet<String>();
      }
      if (consumedByClientIds != null) {
        m_consumedByClientId.addAll(consumedByClientIds);
      }
    }
  }

  /**
   * Returns the Id of the QueueElement
   * 
   * @return the elementId
   */
  public String getElementId() {
    return elementId;
  }

}
