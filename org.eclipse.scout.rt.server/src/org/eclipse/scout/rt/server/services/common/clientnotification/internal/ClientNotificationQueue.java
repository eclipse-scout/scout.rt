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
import java.util.WeakHashMap;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationQueueListener;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 * Serverside blocking queue to keep track of pending client notifications.
 */
public class ClientNotificationQueue {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationQueue.class);
  private final EventListenerList m_listenerList = new EventListenerList();

  private final LinkedList<QueueElement> m_queue;
  private final Object m_queueLock = new Object();

  public ClientNotificationQueue() {
    m_queue = new LinkedList<QueueElement>();
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
      QueueElement newElem = new QueueElement(notification, filter);
      replaceExistingElements(newElem);
      m_queue.add(newElem);
      m_queueLock.notifyAll();
    }
    fireEvent(notification, filter);
  }

  private void replaceExistingElements(QueueElement newElem) {
    for (Iterator<QueueElement> it = m_queue.iterator(); it.hasNext();) {
      QueueElement existingElem = it.next();
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
          for (Iterator<QueueElement> it = m_queue.iterator(); it.hasNext();) {
            QueueElement e = it.next();
            if (!e.isActive()) {
              it.remove();
            }
            else if (e.isConsumable(serverSession)) {
              list.add(e.getClientNotification());
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

  private class QueueElement {
    private final IClientNotification m_notification;
    private final IClientNotificationFilter m_filter;
    private final Object m_consumedBySessionsLock;
    private WeakHashMap<IServerSession, Object> m_consumedBySessions;
    private final long m_valid_until;

    public QueueElement(IClientNotification notification, IClientNotificationFilter filter) {
      m_notification = notification;
      m_filter = filter;
      m_consumedBySessionsLock = new Object();
      m_valid_until = System.currentTimeMillis() + notification.getTimeout();
    }

    public IClientNotification getClientNotification() {
      return m_notification;
    }

    public IClientNotificationFilter getFilter() {
      return m_filter;
    }

    public boolean isActive() {
      return !isExpired() && m_filter.isActive();
    }

    private boolean isExpired() {
      return System.currentTimeMillis() >= m_valid_until;
    }

    private boolean isReplacableBy(QueueElement newElem) {
      return (m_notification == newElem.getClientNotification())
          || (
          newElem.getClientNotification().getClass() == m_notification.getClass()
              && newElem.getFilter().equals(m_filter)
              && newElem.getClientNotification().coalesce(m_notification));
    }

    private boolean isConsumable(IServerSession serverSession) {
      return isActive() && !isConsumedBy(serverSession) && m_filter.accept();
    }

    /**
     * @return true if this notifcation is already consumed by the session
     *         specified
     */
    public boolean isConsumedBy(IServerSession session) {
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
        for (Iterator<QueueElement> it = m_queue.iterator(); it.hasNext();) {
          QueueElement e = it.next();
          if (e.isConsumable(serverSession)
              && consumedNotificationIds.contains(e.getClientNotification().getId())) {
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
