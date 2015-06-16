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

/**
 *
 */
public abstract class AbstractObservableNotificationHandler<T extends Serializable> implements INotificationHandler<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractObservableNotificationHandler.class);

  private final EventListenerList m_globalListeners = new EventListenerList();
  private final Map<ISession, EventListenerList> m_listeners = new WeakHashMap<>();

  public void addGlobalListener(INotificationListener<T> listener) {
    m_globalListeners.add(INotificationListener.class, listener);
  }

  public void removeGlobalListeners(INotificationListener<T> listener) {
    m_globalListeners.remove(INotificationListener.class, listener);
  }

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

  protected void notifiyListeners(T notification) {
    ISession session = ISession.CURRENT.get();
    if (session == null) {
      notifyGlobalListeners(notification);
    }
    else {
      notifySessionBasedListeners(session, notification);
    }
  }

  /**
   * @param notification
   */
  @SuppressWarnings("unchecked")
  protected void notifyGlobalListeners(T notification) {
    for (INotificationListener<T> l : m_globalListeners.getListeners(INotificationListener.class)) {
      try {
        l.handleNotification(notification);
      }
      catch (Exception e) {
        LOG.error(String.format("Error during notification of global listener '%s'.", l), e);
      }
    }
  }

  /**
   * @param notification
   */
  @SuppressWarnings("unchecked")
  protected void notifySessionBasedListeners(ISession session, T notification) {
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
}
