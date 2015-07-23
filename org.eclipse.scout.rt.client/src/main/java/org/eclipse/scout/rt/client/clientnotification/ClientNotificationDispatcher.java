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
package org.eclipse.scout.rt.client.clientnotification;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneCallback;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;

/**
 * Dispatches notifications on the client side
 */
@ApplicationScoped
public class ClientNotificationDispatcher {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationDispatcher.class);

  private final Set<IFuture<Void>> m_notificationFutures = new HashSet<>();

  public void dispatchNotifications(List<ClientNotificationMessage> notifications) {
    IClientSessionRegistry notificationService = BEANS.get(IClientSessionRegistry.class);
    if (notifications == null) {
      LOG.error("Notifications null. Please check your configuration");
      return;
    }

    for (ClientNotificationMessage message : notifications) {
      ClientNotificationAddress address = message.getAddress();
      Serializable notification = message.getNotification();

      if (address.isNotifyAllNodes()) {
        // notify all nodes
        dispatch(notification);
      }
      else if (address.isNotifyAllSessions()) {
        // notify all sessions
        for (IClientSession session : notificationService.getAllClientSessions()) {
          dispatch(session, notification);
        }
      }
      else if (CollectionUtility.hasElements(address.getSessionIds())) {
        // notify all specified sessions
        for (String sessionId : address.getSessionIds()) {
          IClientSession session = notificationService.getClientSession(sessionId);
          if (session == null) {
            LOG.warn(String.format("received notification for invalid session '%s'.", sessionId));
          }
          else {
            dispatch(session, notification);
          }
        }
      }
      else if (CollectionUtility.hasElements(address.getUserIds())) {
        for (String userId : address.getUserIds()) {
          for (IClientSession session : notificationService.getClientSessionsForUser(userId)) {
            dispatch(session, notification);
          }
        }
      }
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
  public void dispatch(final Serializable notification) {

    if (IClientSession.CURRENT.get() != null) {
      // dispatch sync for piggyback notifications
      dispatchSync(notification);
    }
    else {
      // dispatch async
      IRunnable dispatchRunnable = new IRunnable() {

        @Override
        public void run() throws Exception {
          dispatchSync(notification);
        }
      };
      IFuture<Void> future = Jobs.schedule(dispatchRunnable, Jobs.newInput(ClientRunContexts.empty()));
      synchronized (m_notificationFutures) {
        m_notificationFutures.add(future);
        future.whenDone(new P_NotificationFutureCallback(future));
      }

    }
//    P_DispatchRunnable dispatchJob = new P_DispatchRunnable(notification);
    // schedule
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
  public void dispatch(IClientSession session, final Serializable notification) {
//    P_DispatchRunnable dispatchJob = new P_DispatchRunnable(notification);
    ISession currentSession = ISession.CURRENT.get();
    // sync dispatch if session is equal
    if (session == currentSession) {
      dispatchSync(notification);
//      ClientRunContexts.copyCurrent().run(dispatchJob, BEANS.get(RuntimeExceptionTranslator.class));
    }
    else {
      IRunnable dispatchRunnable = new IRunnable() {
        @Override
        public void run() throws Exception {
          dispatchSync(notification);
        }
      };
      IFuture<Void> future = ClientJobs.schedule(dispatchRunnable, ClientJobs.newInput(ClientRunContexts.empty().withSession(session, true)));
      synchronized (m_notificationFutures) {
        m_notificationFutures.add(future);
        future.whenDone(new P_NotificationFutureCallback(future));
      }
    }

//    P_DispatchRunnable dispatchJob = new P_DispatchRunnable(notification);
//    IFuture<Void> future = ClientJobs.schedule(dispatchJob, ClientJobs.newInput(ClientRunContexts.empty().withSession(session, true)));
//    synchronized (m_notificationFutures) {
//      m_notificationFutures.add(future);
//      future.whenDone(new P_NotificationFutureCallback(future));
//    }
  }

  protected void dispatchSync(Serializable notification) {
    NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
    reg.notifyHandlers(notification);
  }

  /**
   * This method should only be used for debugging or test reasons. It waits for all notification jobs to be executed.
   *
   * @throws ProcessingException
   */
  public void waitForPendingNotifications() throws ProcessingException {
    final Set<IFuture<?>> futures = new HashSet<>();
    synchronized (m_notificationFutures) {
      futures.addAll(m_notificationFutures);
    }
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchFutures(futures).andMatchNotCurrentFuture(), Integer.MAX_VALUE, TimeUnit.SECONDS);
  }

//  /**
//   * The runnable is executed in a {@link ClientRunContext} running under a user session. All handlers with a message
//   * type assignable from the notification type will be called to process the notification.
//   */
//  private class P_DispatchRunnable implements IRunnable {
//
//    private final Serializable m_notification;
//
//    public P_DispatchRunnable(Serializable notification) {
//      m_notification = notification;
//    }
//
//    @Override
//    public void run() throws Exception {
//      NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
//      reg.notifyHandlers(m_notification);
//    }
//  }

  private class P_NotificationFutureCallback implements IDoneCallback<Void> {
    private IFuture<Void> m_furture;

    /**
     *
     */
    public P_NotificationFutureCallback(IFuture<Void> furture) {
      m_furture = furture;
    }

    @Override
    public void onDone(DoneEvent<Void> event) {
      synchronized (m_notificationFutures) {
        m_notificationFutures.remove(m_furture);
      }
    }
  }
}
