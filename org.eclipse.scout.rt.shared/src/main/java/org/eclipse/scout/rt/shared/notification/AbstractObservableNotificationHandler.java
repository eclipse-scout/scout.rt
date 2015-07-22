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
package org.eclipse.scout.rt.shared.notification;

import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.session.IGlobalSessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;

/**
 *
 */
public abstract class AbstractObservableNotificationHandler<T extends Serializable> implements INotificationHandler<T>, IGlobalSessionListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractObservableNotificationHandler.class);

  private final Map<ISession, EventListenerList> m_listeners = new WeakHashMap<>();

  /**
   * @param listener
   */
  public void addListener(INotificationListener<T> listener) {
    addListener(ISession.CURRENT.get(), listener);
  }

  public void addListener(ISession session, INotificationListener<T> listener) {
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
    removeListener(ISession.CURRENT.get(), listener);
  }

  public void removeListener(ISession session, INotificationListener<T> listener) {
    synchronized (m_listeners) {
      EventListenerList listeners = m_listeners.get(Assertions.assertNotNull(session));
      if (listeners != null) {
        listeners.remove(INotificationListener.class, listener);
      }
    }
  }

  @Override
  public void handleNotification(T notification) {
    notifiyListeners(notification);
  }

  @SuppressWarnings("unchecked")
  protected void notifiyListeners(T notification) {
    ISession session = ISession.CURRENT.get();

    final INotificationListener<T>[] listeners;
    synchronized (m_listeners) {
      EventListenerList list = m_listeners.get(session);
      if (list != null) {
        listeners = list.getListeners(INotificationListener.class);
      }
      else {
        return;
      }
    }
    for (INotificationListener<T> l : listeners) {
      try {
        l.handleNotification(notification);
      }
      catch (Exception e) {
        LOG.error(String.format("Error during notification of listener '%s'.", l), e);
      }
    }
  }

  @Override
  public void sessionChanged(SessionEvent event) {
    if (event.getType() != SessionEvent.TYPE_STOPPED) {
      // only interested in session stopped
      return;
    }

    synchronized (m_listeners) {
      EventListenerList listeners = m_listeners.get(Assertions.assertNotNull(event.getSource()));
      if (listeners == null) {
        return;
      }

      INotificationListener[] notificationListeners = listeners.getListeners(INotificationListener.class);
      if (notificationListeners == null || notificationListeners.length == 0) {
        return;
      }

      for (INotificationListener<?> notificationListener : notificationListeners) {
        LOG.warn("Auto fallback removal of session listener due to stopped session. This must be done explicitly by the one that registered the listener: " + notificationListener);
        listeners.remove(INotificationListener.class, notificationListener);
      }
    }
  }
}
