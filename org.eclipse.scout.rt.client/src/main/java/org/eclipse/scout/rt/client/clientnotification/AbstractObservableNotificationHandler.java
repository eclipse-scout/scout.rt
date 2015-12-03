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
package org.eclipse.scout.rt.client.clientnotification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.session.IGlobalSessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractObservableNotificationHandler<T extends Serializable> implements INotificationHandler<T>, IGlobalSessionListener {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractObservableNotificationHandler.class);

  private final Map<IClientSession, EventListenerList> m_listeners = new WeakHashMap<>();

  /**
   * @param listener
   */
  public void addListener(INotificationListener<T> listener) {
    addListener(ClientSessionProvider.currentSession(), listener);
  }

  public void addListener(IClientSession session, INotificationListener<T> listener) {
    synchronized (m_listeners) {
      EventListenerList listeners = m_listeners.get(Assertions.assertNotNull(session));
      if (listeners == null) {
        listeners = new EventListenerList();
        m_listeners.put(session, listeners);
      }
      listeners.add(INotificationListener.class, listener);
    }
  }

  public void removeListener(INotificationListener<T> listener) {
    removeListener(ClientSessionProvider.currentSession(), listener);
  }

  public void removeListener(IClientSession session, INotificationListener<T> listener) {
    synchronized (m_listeners) {
      EventListenerList listeners = m_listeners.get(Assertions.assertNotNull(session));
      if (listeners != null) {
        listeners.remove(INotificationListener.class, listener);
        if (listeners.getListenerCount(INotificationListener.class) == 0) {
          m_listeners.remove(session);
        }
      }
    }
  }

  @Override
  public void handleNotification(T notification) {
    notifyListeners(notification);
  }

  protected void notifyListeners(T notification) {
    IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      notifyListenersWithoutCurrentSession(notification);
    }
    else {
      EventListenerList list;
      synchronized (m_listeners) {
        list = m_listeners.get(session);
      }
      notifyListenersWithCurrentSession(notification, session, list);
    }
  }

  protected void notifyListenersWithoutCurrentSession(final T notification) {
    // create copy of m_listeners (EventListenerList is thread-safe)
    Map<IClientSession, EventListenerList> listenerMap;
    synchronized (m_listeners) {
      listenerMap = new HashMap<>(m_listeners);
    }
    for (Entry<IClientSession, EventListenerList> entry : listenerMap.entrySet()) {
      final IClientSession session = entry.getKey();
      final EventListenerList list = entry.getValue();
      if (list != null && list.getListenerCount(INotificationListener.class) > 0) {
        ModelJobs.schedule(new IRunnable() {
          @Override
          public void run() throws Exception {
            notifyListenersWithCurrentSession(notification, session, list);
          }
        }, ModelJobs.newInput(ClientRunContexts.empty().withSession(session, true)));
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void notifyListenersWithCurrentSession(T notification, IClientSession session, EventListenerList list) {
    final INotificationListener<T>[] listeners;
    if (list != null) {
      listeners = list.getListeners(INotificationListener.class);
    }
    else {
      return;
    }

    for (INotificationListener<T> l : listeners) {
      l.handleNotification(notification);
    }
  }

  @Override
  public void sessionChanged(SessionEvent event) {
    if (event.getType() != SessionEvent.TYPE_STOPPED) {
      // only interested in session stopped
      return;
    }
    synchronized (m_listeners) {
      ISession session = Assertions.assertNotNull(event.getSource());
      EventListenerList listeners = m_listeners.get(session);
      if (listeners == null) {
        return;
      }

      @SuppressWarnings("unchecked")
      INotificationListener<T>[] notificationListeners = listeners.getListeners(INotificationListener.class);
      if (notificationListeners == null || notificationListeners.length == 0) {
        return;
      }

      for (INotificationListener<T> notificationListener : notificationListeners) {
        LOG.warn("Auto fallback removal of session listener due to stopped session. This must be done explicitly by the one that registered the listener: " + notificationListener);
        if (session instanceof IClientSession) {
          removeListener((IClientSession) session, notificationListener);
        }
      }
    }
  }
}
