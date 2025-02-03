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
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
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
    if (notifications == null) {
      LOG.warn("Notifications are null. Please check your configuration.");
      return;
    }

    IClientSessionRegistry notificationService = BEANS.get(IClientSessionRegistry.class);
    for (ClientNotificationMessage message : notifications) {
      RunContexts.copyCurrent()
          .withCorrelationId(message.getCorrelationId())
          .run(() -> dispatchNotification(message, notificationService));
    }
  }

  protected void dispatchNotification(ClientNotificationMessage message, IClientSessionRegistry notificationService) {
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
        if (isSessionValid(session, session.getId())) {
          dispatchForSession(session, notification, address);
        }
      }
    }
    else if (CollectionUtility.hasElements(address.getSessionIds())) {
      // notify all specified sessions
      LOG.debug("Notify sessions by session ids: {}", address.getSessionIds());
      for (String sessionId : address.getSessionIds()) {
        IClientSession session = notificationService.getClientSession(sessionId);
        if (isSessionValid(session, sessionId)) {
          dispatchForSession(session, notification, address);
        }
      }
    }
    else if (CollectionUtility.hasElements(address.getUserIds())) {
      LOG.debug("Notify sessions by user ids: {}", address.getUserIds());
      for (String userId : address.getUserIds()) {
        for (IClientSession session : notificationService.getClientSessionsForUser(userId)) {
          if (isSessionValid(session, session.getId())) {
            dispatchForSession(session, notification, address);
          }
        }
      }
    }
  }

  protected boolean isSessionValid(IClientSession session, String sessionId) {
    if (session == null) {
      LOG.debug("Received notification could not be delivered because the target session '{}' could not be found.", sessionId);
      return false;
    }
    if (session.getId() == null) {
      // If the id of a session is not yet set, the ISession.start(String) has not yet been called.
      // Do not dispatch notification for such uninitialized sessions. They have just been created and are not yet ready to receive notifications.
      // As soon as start() has been called (which happens in a ModelJob), the session may be used for notifications.
      // As notifications modifying e.g. the Desktop need to be executed in a ModelJob too, such notification will only be handled after the start of the session has been completed.
      LOG.debug("Received notification for session '{}' is ignored because the session is not starting or started (active).", sessionId);
      return false;
    }
    return true;
  }

  /**
   * the notification will be applied sync if the method invocation is done in with a {@link IClientSession} in the
   * {@link RunContext}. The sync execution is due to piggyback notifications expected to be applied after a successful
   * backend call. In case no {@link IClientSession} is in the current {@link RunContext} the notification is applied
   * async.
   */
  public void dispatchForNode(Serializable notification, IClientNotificationAddress address) {
    if (IClientSession.CURRENT.get() != null) {
      // dispatch sync for piggyback notifications
      dispatchSync(notification, address);
    }
    else {
      // dispatch async
      IFuture<Void> future = Jobs.schedule(() -> dispatchSync(notification, address), Jobs.newInput()
          .withRunContext(ClientRunContexts.copyCurrent()
              .withTransactionScope(TransactionScope.REQUIRES_NEW))
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
   *     the session describes the {@link RunContext} in which the notification should be processed.
   * @param notification
   *     the notification to process.
   */
  public void dispatchForSession(IClientSession session, Serializable notification, IClientNotificationAddress address) {
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
   * @return pending notification futures to be able to wait for completion.
   */
  public Set<IFuture<?>> getPendingNotifications() {
    synchronized (m_notificationFutures) {
      return new HashSet<>(m_notificationFutures);
    }
  }

  protected void dispatchSync(Serializable notification, IClientNotificationAddress address) {
    NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
    reg.notifyHandlers(notification, address);
  }

  private class P_NotificationFutureCallback implements IDoneHandler<Void> {
    private final IFuture<Void> m_future;

    P_NotificationFutureCallback(IFuture<Void> future) {
      m_future = future;
    }

    @Override
    public void onDone(DoneEvent<Void> event) {
      removePendingNotification(m_future);
    }
  }
}
