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
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationNotification;
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent;
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent.EventType;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationQueueListener;
import org.eclipse.scout.rt.server.services.common.clientnotification.SessionFilter;
import org.eclipse.scout.rt.server.services.common.node.NodeSynchronizationProcessService;
import org.eclipse.scout.rt.server.services.common.notification.INotificationService;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.service.SERVICES;

/**
 * element type used in CTIStateCache
 */
public class ClientNotificationQueue {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationQueue.class);
  private EventListenerList m_listenerList = new EventListenerList();

  private LinkedList<ClientNotificationNotification> m_queue;
  private Object m_queueLock = new Object();

  public ClientNotificationQueue() {
    m_queue = new LinkedList<ClientNotificationNotification>();
  }

  public void putDistributedNotification(IClientNotification notification, IClientNotificationFilter filter) {
    notification.setReceiveingServerNodeId(SERVICES.getService(NodeSynchronizationProcessService.class).getClusterNodeId());

    ClientNotificationNotification clientNotificationNotification = new ClientNotificationNotification(notification, filter);
    putNotification(clientNotificationNotification);

    INotificationService messageService = SERVICES.getService(INotificationService.class);
    if (messageService != null && !(filter instanceof SessionFilter)) {
      messageService.publishNotification(clientNotificationNotification);
    }
  }

  public void putNotification(IClientNotification notification, IClientNotificationFilter filter) {
    notification.setReceiveingServerNodeId(SERVICES.getService(NodeSynchronizationProcessService.class).getClusterNodeId());

    ClientNotificationNotification clientNotificationNotification = new ClientNotificationNotification(notification, filter);
    putNotification(clientNotificationNotification);
  }

  public void putNotification(ClientNotificationNotification notifiationNotification) {
    if (notifiationNotification == null) {
      throw new IllegalArgumentException("notification must not be null");
    }
    if (notifiationNotification.getClientNotification() == null) {
      throw new IllegalArgumentException("client notification must not be null");
    }
    if (notifiationNotification.getFilter() == null) {
      throw new IllegalArgumentException("filter must not be null");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("put " + notifiationNotification.getClientNotification() + " for " + notifiationNotification.getFilter());
    }
    synchronized (m_queueLock) {
      for (Iterator<ClientNotificationNotification> it = m_queue.iterator(); it.hasNext();) {
        ClientNotificationNotification e = it.next();
        if (!e.getFilter().isActive()) {
          it.remove();
        }
        else if (e.getClientNotification() == notifiationNotification.getClientNotification()) {
          it.remove();
        }
        else if (e.getClientNotification().getClass() == notifiationNotification.getClientNotification().getClass() && notifiationNotification.getFilter().equals(e.getFilter()) && notifiationNotification.getClientNotification().coalesce(e.getClientNotification())) {
          it.remove();
        }
      }
      m_queue.add(notifiationNotification);
      m_queueLock.notifyAll();
    }
    fireEvent(notifiationNotification, EventType.NEW);
  }

  public void updateNotification(ClientNotificationNotification notification) {
    synchronized (m_queueLock) {
      for (Iterator<ClientNotificationNotification> it = m_queue.iterator(); it.hasNext();) {
        ClientNotificationNotification e = it.next();
        if (!e.getFilter().isActive()) {
          it.remove();
        }
        else if (e.getClientNotification().getId().equals(notification.getClientNotification().getId())) {
          e.addConsumedBy(notification.getConsumedBy());
        }
      }
    }
  }

  public void removeNotification(ClientNotificationNotification notifiationNotification) {
    synchronized (m_queueLock) {
      for (Iterator<ClientNotificationNotification> it = m_queue.iterator(); it.hasNext();) {
        ClientNotificationNotification e = it.next();
        if (!e.getFilter().isActive()) {
          it.remove();
        }
        else if (!e.getClientNotification().getId().equals(notifiationNotification.getClientNotification().getId())) {
          m_queue.remove(e);
        }
      }
    }
  }

  public Set<IClientNotification> getNextNotifications(long blockingTimeout) {
    long endTime = System.currentTimeMillis() + blockingTimeout;
    Set<IClientNotification> list = new HashSet<IClientNotification>();
    synchronized (m_queueLock) {
      while (true) {
        if (!m_queue.isEmpty()) {
          for (Iterator<ClientNotificationNotification> it = m_queue.iterator(); it.hasNext();) {
            ClientNotificationNotification e = it.next();
            if (e.getFilter().isActive()) {
              IServerSession serverSession = ThreadContext.getServerSession();
              if (!e.isConsumedBy(serverSession.getId())) {
                if (e.getFilter().accept()) {
                  String id = SERVICES.getService(NodeSynchronizationProcessService.class).getClusterNodeId();
                  e.getClientNotification().setProvidingServerNodeId(id);

                  list.add(e.getClientNotification());
                  if (e.getFilter().isMulticast()) {
                    e.setConsumedBy(serverSession.getId());
                    fireEvent(e, EventType.UPDATE);
                    SERVICES.getService(INotificationService.class).updateNotification(e);
                  }
                  else {
                    it.remove();
                    fireEvent(e, EventType.REMOVE);
                    SERVICES.getService(INotificationService.class).removeNotification(e);
                  }
                }
              }
            }
            else {
              it.remove();
              fireEvent(e, EventType.REMOVE);
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
    private IClientNotification m_notification;
    private IClientNotificationFilter m_filter;
    private Object m_consumedBySessionsLock;
    private WeakHashMap<IServerSession, Object> m_consumedBySessions;

    public QueueElement(IClientNotification notification, IClientNotificationFilter filter) {
      m_notification = notification;
      m_filter = filter;
      m_consumedBySessionsLock = new Object();
    }

    public IClientNotification getClientNotification() {
      return m_notification;
    }

    public IClientNotificationFilter getFilter() {
      return m_filter;
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
      if (m_consumedBySessions == null) {
        return false;
      }
      //
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

  private void fireEvent(ClientNotificationNotification notificationNotification, EventType eventType) {
    IClientNotificationQueueListener[] listeners = m_listenerList.getListeners(IClientNotificationQueueListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        (listeners[i]).queueChanged(new ClientNotificationQueueEvent(notificationNotification, eventType));
      }
    }
  }
}
