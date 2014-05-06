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
package org.eclipse.scout.rt.server.services.common.clientnotification.internal;

import java.util.WeakHashMap;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationQueueElement;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 * Serverside clientnotification queue element
 */
public class ConsumableClientNotificationQueueElement extends ClientNotificationQueueElement implements IClientNotificationQueueElement {
  private static final long serialVersionUID = 5831131193589383447L;
  private final Object m_consumedBySessionsLock;
  private WeakHashMap<IServerSession, Object> m_consumedBySessions;

  public ConsumableClientNotificationQueueElement(IClientNotification notification, IClientNotificationFilter filter) {
    super(notification, filter);
    m_consumedBySessionsLock = new Object();
  }

  public boolean isConsumable(IServerSession serverSession) {
    return isActive() && !isConsumedBy(serverSession) && m_filter.accept();
  }

  /**
   * @return true if this notifcation is already consumed by the session
   *         specified
   */
  private boolean isConsumedBy(IServerSession session) {
    // fast check
    if (session == null) {
      return false;
    }
    synchronized (m_consumedBySessionsLock) {
      if (m_consumedBySessions != null) {
        return m_consumedBySessions.containsKey(session);
      }
      else {
        return false;
      }
    }
  }

  /**
   * keeps in mind that this notifcation was consumed by the session specified
   */
  public void setConsumedBy(IServerSession session) {
    if (session != null) {
      synchronized (m_consumedBySessionsLock) {
        if (m_consumedBySessions == null) {
          m_consumedBySessions = new WeakHashMap<IServerSession, Object>();
        }
        m_consumedBySessions.put(session, null);
      }
    }
  }
}
