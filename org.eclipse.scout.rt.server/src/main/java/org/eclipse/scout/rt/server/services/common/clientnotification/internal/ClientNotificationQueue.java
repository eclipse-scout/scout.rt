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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationQueueElement;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationQueueListener;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 * Serverside blocking queue to keep track of pending client notifications.
 */
public class ClientNotificationQueue {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationQueue.class);
  private final EventListenerList m_listenerList = new EventListenerList();

  private final LinkedList<ConsumableClientNotificationQueueElement> m_queue;
  private final Object m_queueLock = new Object();

  public ClientNotificationQueue() {
    m_queue = new LinkedList<ConsumableClientNotificationQueueElement>();
  }

  public void putNotification(IClientNotification notification, IClientNotificationFilter filter) {
    if (notification == null) {
      throw new IllegalArgumentException("notification must not be null");
    }
    if (filter == null) {
      throw new IllegalArgumentException("filter must not be null");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("put " + notification + " for " + filter);
    }
    synchronized (m_queueLock) {
      ConsumableClientNotificationQueueElement newElem = new ConsumableClientNotificationQueueElement(notification, filter);
      replaceExistingElements(newElem);
      m_queue.add(newElem);
      m_queueLock.notifyAll();
    }
    fireEvent(notification, filter);
  }

  private void replaceExistingElements(IClientNotificationQueueElement newElem) {
    for (Iterator<ConsumableClientNotificationQueueElement> it = m_queue.iterator(); it.hasNext();) {
      ClientNotificationQueueElement existingElem = it.next();
      if (!existingElem.isActive() || existingElem.isReplacableBy(newElem)) {
        it.remove();
      }
    }
  }

  public Set<IClientNotification> getNextNotifications(long blockingTimeout) {
    long endTime = System.currentTimeMillis() + blockingTimeout;
    Set<IClientNotification> list = new HashSet<IClientNotification>();
    synchronized (m_queueLock) {
      while (true) {
        if (!m_queue.isEmpty()) {
          IServerSession serverSession = ThreadContext.getServerSession();
          for (Iterator<ConsumableClientNotificationQueueElement> it = m_queue.iterator(); it.hasNext();) {
            ConsumableClientNotificationQueueElement e = it.next();
            if (!e.isActive()) {
              it.remove();
            }
            else if (e.isConsumable(serverSession)) {
              list.add(e.getNotification());
            }
          }
        }
        long dt = endTime - System.currentTimeMillis();
        if (list.size() > 0 || dt <= 0) {
          break;
        }
        else {
          try {
            m_queueLock.wait(dt);
          }
          catch (InterruptedException ie) {
          }
        }
      }
    }
    return list;
  }

  /**
   * Model Observer
   */
  public void addClientNotificationQueueListener(IClientNotificationQueueListener listener) {
    m_listenerList.add(IClientNotificationQueueListener.class, listener);
  }

  public void removeClientNotificationQueueListener(IClientNotificationQueueListener listener) {
    m_listenerList.remove(IClientNotificationQueueListener.class, listener);
  }

  private void fireEvent(IClientNotification notification, IClientNotificationFilter filter) {
    IClientNotificationQueueListener[] listeners = m_listenerList.getListeners(IClientNotificationQueueListener.class);
    for (IClientNotificationQueueListener l : listeners) {
      l.queueChanged(new ClientNotificationQueueEvent(notification, filter, ClientNotificationQueueEvent.TYPE_NOTIFICATION_ADDED));
    }
  }

  public void ackNotifications(Set<String> consumedNotificationIds) {
    synchronized (m_queueLock) {
      if (!m_queue.isEmpty()) {
        IServerSession serverSession = ThreadContext.getServerSession();
        for (Iterator<ConsumableClientNotificationQueueElement> it = m_queue.iterator(); it.hasNext();) {
          ConsumableClientNotificationQueueElement e = it.next();
          if (e.isConsumable(serverSession)
              && consumedNotificationIds.contains(e.getNotification().getId())) {
            e.setConsumedBy(serverSession);
            if (!e.getFilter().isMulticast()) {
              it.remove();
            }
          }
        }
      }
    }
  }
}
