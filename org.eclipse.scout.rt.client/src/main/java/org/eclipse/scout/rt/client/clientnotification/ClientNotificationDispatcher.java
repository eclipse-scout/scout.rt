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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches notifications on the client side
 */
@ApplicationScoped
public class ClientNotificationDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationDispatcher.class);

  private final Set<IFuture<Void>> m_notificationFutures = new HashSet<>();

  public void dispatchNotifications(List<ClientNotificationMessage> notifications) {
    final IClientSessionRegistry notificationService = BEANS.get(IClientSessionRegistry.class);
    if (notifications == null) {
      LOG.error("Notifications null. Please check your configuration");
      return;
    }

    for (final ClientNotificationMessage message : notifications) {
      RunContexts.copyCurrent()
          .withCorrelationId(message.getCorrelationId())
          .run(() -> {
            IClientNotificationAddress address = message.getAddress();
            Serializable notification = message.getNotification();
            LOG.debug("Processing client notification {}", notification);

            if (address.isNotifyAllNodes()) {
              // notify all nodes
              LOG.debug("Notify all nodes");
              dispatchForNode(notification, address);
            }
            else if (address.isNotifyAllSessions()) {
              // notify all sessions
              LOG.debug("Notify all sessions");
              for (IClientSession session : notificationService.getAllClientSessions()) {
                dispatchForSession(session, notification, address);
              }
            }
            else if (CollectionUtility.hasElements(address.getSessionIds())) {
              // notify all specified sessions
              LOG.debug("Notify sessions by session id: {}", address.getSessionIds());
              for (String sessionId : address.getSessionIds()) {
                IClientSession session = notificationService.getClientSession(sessionId);
                if (session == null) {
                  LOG.warn("received notification for invalid session '{}'.", sessionId);
                }
                else {
                  dispatchForSession(session, notification, address);
                }
              }
            }
            else if (CollectionUtility.hasElements(address.getUserIds())) {
              LOG.debug("Notify sessions by user id: {}", address.getUserIds());
              for (String userId : address.getUserIds()) {
                for (IClientSession session : notificationService.getClientSessionsForUser(userId)) {
                  dispatchForSession(session, notification, address);
                }
              }
            }
          });
    }
  }

  /**
   * the notification will be applied sync if the method invocation is done in with a {@link IClientSession} in the
   * {@link RunContext}. The sync execution is due to piggyback notifications expected to be applied after a successful
   * backendcall. In case no {@link IClientSession} is in the current {@link RunContext} the notification is applied
   * async.
   *
   * @param notification
   */
  public void dispatchForNode(final Serializable notification, final IClientNotificationAddress address) {
    if (IClientSession.CURRENT.get() != null) {
      // dispatch sync for piggyback notifications
      dispatchSync(notification, address);
    }
    else {
      // dispatch async
      IFuture<Void> future = Jobs.schedule(() -> dispatchSync(notification, address), Jobs.newInput()
          .withRunContext(ClientRunContexts.copyCurrent())
          .withName("Dispatching client notification"));

      addPendingNotification(future);

      future.whenDone(new P_NotificationFutureCallback(future), null);
    }
  }

  /**
   * Dispatch notifications within the context of a session.<br>
   * Dispatching is always done asynchronously to ensure that it is not handled within a model thread.
   *
   * @param session
   *          the session describes the runcontext in which the notification should be processed.
   * @param notification
   *          the notification to process.
   */
  public void dispatchForSession(IClientSession session, final Serializable notification, final IClientNotificationAddress address) {
    ISession currentSession = ISession.CURRENT.get();
    // sync dispatch if session is equal
    if (session == currentSession) {
      dispatchSync(notification, address);
    }
    else {
      IFuture<Void> future = Jobs.schedule(() -> dispatchSync(notification, address), Jobs.newInput()
          .withRunContext(ClientRunContexts.empty()
              .withSession(session, true)
              .withCorrelationId(CorrelationId.CURRENT.get()))
          .withName("Dispatching client notification"));

      addPendingNotification(future);

      future.whenDone(new P_NotificationFutureCallback(future), null);
    }
  }

  /**
   * keep pending notification future to be able to wait for completion.
   */
  protected void addPendingNotification(IFuture<Void> future) {
    synchronized (m_notificationFutures) {
      m_notificationFutures.add(future);
    }
  }

  /**
   * keep pending notification future
   */
  protected void removePendingNotification(IFuture<Void> future) {
    synchronized (m_notificationFutures) {
      m_notificationFutures.remove(future);
    }
  }

  /**
   * @return pending notification futures to be able to to wait for completion.
   */
  public Set<IFuture<?>> getPendingNotifications() {
    final Set<IFuture<?>> futures = new HashSet<>();
    synchronized (m_notificationFutures) {
      futures.addAll(m_notificationFutures);
      return futures;
    }
  }

  protected void dispatchSync(Serializable notification, IClientNotificationAddress address) {
    NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
    reg.notifyHandlers(notification, address);
  }

  private class P_NotificationFutureCallback implements IDoneHandler<Void> {
    private final IFuture<Void> m_future;

    P_NotificationFutureCallback(IFuture<Void> furture) {
      m_future = furture;
    }

    @Override
    public void onDone(DoneEvent<Void> event) {
      removePendingNotification(m_future);
    }
  }
}
