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
package org.eclipse.scout.rt.client.services.common.clientnotification.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.service.AbstractService;

/**
 * * A service to dispatch incoming client notifications (from the server) to
 * {@link IClientNotificationConsumerListener} listeners.
 * <p>
 * Keeps track of consumed notification ids until the notification expires. Listeners are only notified once that a
 * notification is arrived.
 * </p>
 */
@Priority(-3)
public class ClientNotificationConsumerService extends AbstractService implements IClientNotificationConsumerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationConsumerService.class);
  private static final String SESSION_DATA_KEY = "clientNotificationConsumerServiceState";

  private final ServiceState m_globalServiceState = new ServiceState();

  private ServiceState getServiceState(IClientSession session) {
    if (session == null) {
      throw new IllegalStateException("session is null");
    }
    synchronized (session) {
      ServiceState data = (ServiceState) session.getData(SESSION_DATA_KEY);
      if (data == null) {
        data = new ServiceState();
        session.setData(SESSION_DATA_KEY, data);
      }
      return data;
    }
  }

  @Override
  public void dispatchClientNotifications(final IClientNotification[] notifications, final IClientSession session) {
    if (notifications == null || notifications.length == 0) {
      return;
    }
    if (ClientJob.getCurrentSession() == session) {
      // we are sync
      fireEvent(session, notifications, true);
    }
    else {
      // async
      new ClientAsyncJob("Dispatch client notifications", session) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          fireEvent(session, notifications, false);
        }
      }.schedule();
    }
  }

  private <T> HashSet<T> hashSetWithoutNullElements(Collection<? extends T> c) {
    @SuppressWarnings("unchecked")
    HashSet<T> set = (HashSet<T>) CollectionUtility.hashSet(c);
    set.remove(null);
    return set;
  }

  private void fireEvent(final IClientSession session, final IClientNotification[] notifications, boolean sync) {
    getServiceState(session).fireEvent(notifications, sync, this);
    m_globalServiceState.fireEvent(notifications, sync, this);
  }

  @Override
  public void addClientNotificationConsumerListener(IClientSession session, IClientNotificationConsumerListener listener) {
    getServiceState(session).addListener(listener);
  }

  @Override
  public void removeClientNotificationConsumerListener(IClientSession session, IClientNotificationConsumerListener listener) {
    getServiceState(session).removeListener(listener);
  }

  @Override
  public void addGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    m_globalServiceState.addListener(listener);
  }

  @Override
  public void removeGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    m_globalServiceState.removeListener(listener);
  }

  @Override
  public Set<String> getConsumedNotificationIds(final IClientSession session) {
    return getServiceState(session).getConsumedIds();
  }

  @Override
  public Set<String> getGlobalConsumedNotificationIds() {
    return m_globalServiceState.getConsumedIds();
  }

  public void removeConsumedNotificationIds(final Set<String> cnIds, final IClientSession session) {
    getServiceState(session).removeConsumedIds(cnIds);
  }

  public void removeGlobalConsumedNotificationIds(final Set<String> cnIds) {
    m_globalServiceState.removeConsumedIds(cnIds);
  }

  /**
   * Stores already consumed notification ids and registered listeners.
   */
  private static class ServiceState {
    private final EventListenerList m_listenerList = new EventListenerList();

    private final ConcurrentHashMap<String/*notification id*/, Long /*timeout*/> m_consumedIds = new ConcurrentHashMap<String, Long>();

    @SuppressWarnings("unchecked")
    public Set<String> getConsumedIds() {
      return Collections.unmodifiableSet(new HashSet(m_consumedIds.keySet()));
    }

    public void removeConsumedIds(Collection<String> cnIds) {
      for (String id : cnIds) {
        m_consumedIds.remove(id);
      }
    }

    public void addListener(IClientNotificationConsumerListener listener) {
      m_listenerList.add(IClientNotificationConsumerListener.class, listener);
    }

    public void removeListener(IClientNotificationConsumerListener listener) {
      m_listenerList.remove(IClientNotificationConsumerListener.class, listener);
    }

    public void fireEvent(final IClientNotification[] notifications, boolean sync, IClientNotificationConsumerService service) {
      for (IClientNotification n : notifications) {
        ClientNotificationConsumerEvent event = new ClientNotificationConsumerEvent(service, n);
        fireEvent(n, sync, event);
      }
      cleanupExpiredNotifications();
    }

    private void fireEvent(IClientNotification notification, boolean sync, ClientNotificationConsumerEvent e) {
      Long validUntil = Long.valueOf(System.currentTimeMillis() + notification.getTimeout());
      Long previousValue = m_consumedIds.putIfAbsent(notification.getId(), validUntil);
      if (previousValue == null) {
        fireEventInternal(notification, sync, e);
      }
    }

    private void fireEventInternal(IClientNotification notification, boolean sync, ClientNotificationConsumerEvent e) {
      IClientNotificationConsumerListener[] listeners = m_listenerList.getListeners(IClientNotificationConsumerListener.class);
      for (IClientNotificationConsumerListener l : listeners) {
        try {
          l.handleEvent(e, sync);
        }
        catch (Throwable t) {
          LOG.error("Listener " + l.getClass().getName() + " on event " + notification, t);
        }
      }
    }

    private void cleanupExpiredNotifications() {
      for (Entry<String, Long> e : m_consumedIds.entrySet()) {
        if (isExpired(e.getValue())) {
          m_consumedIds.remove(e.getKey());
        }
      }
    }

    private boolean isExpired(long validUntil) {
      return System.currentTimeMillis() >= validUntil;
    }
  }

}
