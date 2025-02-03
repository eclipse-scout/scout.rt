/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.clientnotification;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.IDispatchingNotificationHandler;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.session.IGlobalSessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A notification handler allowing to register {@link INotificationListener}s for specific sessions.
 * <p>
 * Listeners can be called within the context of the given session upon receipt of a notification.
 * {@link INotificationListener#handleNotification(Serializable)} is not called within a model job (see
 * {@link ModelJobs})
 * </p>
 */
public abstract class AbstractObservableNotificationHandler<T extends Serializable> implements IDispatchingNotificationHandler<T>, IGlobalSessionListener {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractObservableNotificationHandler.class);

  private final Map<IClientSession, FastListenerList<INotificationListener<T>>> m_listeners = new WeakHashMap<>();

  /**
   * Add a notification listener for notifications of type T. Upon receipt of a notification,
   * {@link INotificationListener#handleNotification(Serializable)} is called within a job with the current
   * {@link IClientSession}.
   *
   * @param listener
   *     {@link INotificationListener}
   */
  public void addListener(INotificationListener<T> listener) {
    addListener(ClientSessionProvider.currentSession(), listener);
  }

  /**
   * Add a notification listener for notifications of type T. Upon receipt of a notification,
   * {@link INotificationListener#handleNotification(Serializable)} is called within a model job with the given
   * {@link IClientSession}.
   *
   * @param listener
   *     {@link INotificationListener}
   * @param session
   *     {@link IClientSession}
   */
  public void addListener(IClientSession session, INotificationListener<T> listener) {
    Assertions.assertNotNull(session, "client session can not be null");
    Assertions.assertNotNull(listener, "listener can not be null");
    synchronized (m_listeners) {
      FastListenerList<INotificationListener<T>> listeners = m_listeners.get(Assertions.assertNotNull(session));
      if (listeners == null) {
        listeners = new FastListenerList<>();
        m_listeners.put(session, listeners);
      }
      listeners.add(listener);
    }
  }

  /**
   * Removes the given listener for the current session.
   *
   * @see #addListener(INotificationListener)
   */
  public void removeListener(INotificationListener<T> listener) {
    removeListener(ClientSessionProvider.currentSession(), listener);
  }

  /**
   * Removes the given listener for the given session.
   *
   * @see #addListener(IClientSession, INotificationListener)
   */
  public void removeListener(IClientSession session, INotificationListener<T> listener) {
    Assertions.assertNotNull(session, "client session can not be null");
    Assertions.assertNotNull(listener, "listener can not be null");
    synchronized (m_listeners) {
      FastListenerList<INotificationListener<T>> listeners = m_listeners.get(session);
      if (listeners != null) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
          m_listeners.remove(session);
        }
      }
    }
  }

  /**
   * Returns the {@link INotificationListener}s for a given session.
   *
   * @param session
   *     {@link IClientSession}
   * @return {@link INotificationListener}s
   */
  public List<INotificationListener<T>> getListeners(IClientSession session) {
    synchronized (m_listeners) {
      FastListenerList<INotificationListener<T>> listeners = m_listeners.get(session);
      if (listeners != null) {
        return listeners.list();
      }
      else {
        return Collections.emptyList();
      }
    }
  }

  protected FastListenerList<INotificationListener<T>> getListenerList(IClientSession session) {
    synchronized (m_listeners) {
      return m_listeners.get(session);
    }
  }

  @Override
  public void handleNotification(T notification, IClientNotificationAddress address) {
    if (address.isNotifyAllNodes()) {
      notifyListenersOfAllSessions(notification);
    }
    else {
      notifyListenersOfCurrentSession(notification);
    }
  }

  /**
   * Notify all listeners independent of the session in the current {@link RunContext}
   */
  protected void notifyListenersOfAllSessions(final T notification) {
    // create copy of m_listeners (EventListenerList is thread-safe)
    Map<IClientSession, FastListenerList<INotificationListener<T>>> listenerCopy;
    synchronized (m_listeners) {
      listenerCopy = new HashMap<>(m_listeners);
    }

    //schedule model job per session to handle notifications
    listenerCopy.forEach((session, listenerList) -> scheduleHandlingNotifications(notification, listenerList, session));
  }

  /**
   * Only notify listeners of the current session (e.g. if a notification is only addressed to certain users or
   * sessions)
   */
  protected void notifyListenersOfCurrentSession(final T notification) {
    ISession currentSession = IClientSession.CURRENT.get();
    if (currentSession instanceof IClientSession) {
      IClientSession session = (IClientSession) currentSession;
      scheduleHandlingNotifications(notification, getListenerList(session), session);
    }
  }

  protected void scheduleHandlingNotifications(final T notification, final FastListenerList<INotificationListener<T>> listenerList, final IClientSession session) {
    if (listenerList == null) {
      return;
    }

    JobInput input = Jobs.newInput()
        .withName("Handling Client Notification")
        .withRunContext(ClientRunContexts.empty()
            .withSession(session, true)
            .withCorrelationId(CorrelationId.CURRENT.get()));
    Jobs.schedule(() -> listenerList.list().forEach(listener -> listener.handleNotification(notification)), input);
  }

  /**
   * Automatically removes listeners for stopped sessions.
   */
  @Override
  public void sessionChanged(SessionEvent event) {
    if (event.getType() == SessionEvent.TYPE_STOPPED) {
      // only interested in session stopped
      ISession session = Assertions.assertNotNull(event.getSource());
      if (session instanceof IClientSession) {
        IClientSession clientSession = (IClientSession) session;
        synchronized (m_listeners) {
          for (INotificationListener<T> notificationListener : getListeners(clientSession)) {
            removeListener(clientSession, notificationListener);
            LOG.warn("Auto fallback removal of session listener due to stopped session. This must be done explicitly by the one that registered the listener: {}", notificationListener);
          }
        }
      }
    }
  }
}
