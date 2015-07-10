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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.RuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneCallback;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;

/**
 *
 */
@ApplicationScoped
public class ClientNotificationDispatcher {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationDispatcher.class);

  private final IFilter<ClientNotificationMessage> ACCEPT_ALL_FILTER = new IFilter<ClientNotificationMessage>() {
    @Override
    public boolean accept(ClientNotificationMessage element) {
      return true;
    }
  };

  private final Set<IFuture<Void>> m_notificationFutures = new HashSet<>();

  public void dispatchNotifications(Collection<ClientNotificationMessage> notifications) {
    dispatchNotifications(notifications, ACCEPT_ALL_FILTER);
  }

  /**
   * @param notifications
   */
  public void dispatchNotifications(Collection<ClientNotificationMessage> notifications, IFilter<ClientNotificationMessage> filter) {
    IClientSessionRegistry notificationService = BEANS.get(IClientSessionRegistry.class);
    // FIXME AHO/JGU: hack added by A.WE to prevent NPE
    if (notifications == null) {
      notifications = Collections.emptyList();
    }
    for (ClientNotificationMessage message : notifications) {
      if (!filter.accept(message)) {
        continue;
      }
      if (message.getAddress().isNotifyAllNodes()) {
        dispatch(message.getNotification());
      }
      else if (message.getAddress().isNotifyAllSessions()) {
        // notify all sessions
        for (IClientSession session : notificationService.getAllClientSessions()) {
          dispatch(session, message.getNotification());
        }
      }
      else {
        if (CollectionUtility.hasElements(message.getAddress().getSessionIds())) {
          for (String sessionId : message.getAddress().getSessionIds()) {
            IClientSession session = notificationService.getClientSession(sessionId);
            if (session == null) {
              LOG.warn(String.format("received notification for invalid session '%s'.", sessionId));
            }
            else {
              dispatch(session, message.getNotification());
            }
          }
          if (CollectionUtility.hasElements(message.getAddress().getUserIds())) {
            for (String userId : message.getAddress().getUserIds()) {
              for (IClientSession session : notificationService.getClientSessionsForUser(userId)) {
                dispatch(session, message.getNotification());
              }
            }
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
  public void dispatch(Serializable notification) {
    P_DispatchRunnable dispatchJob = new P_DispatchRunnable(notification);
    ISession currentSession = ISession.CURRENT.get();
    if (currentSession != null) {
      // sync dispatch to ensure applied notification after return
      RunContexts.copyCurrent().run(dispatchJob, BEANS.get(RuntimeExceptionTranslator.class));
    }
    else {
      // schedule
      IFuture<Void> future = Jobs.schedule(dispatchJob, Jobs.newInput(ClientRunContexts.empty()));
      synchronized (m_notificationFutures) {
        m_notificationFutures.add(future);
        future.whenDone(new P_NotificationFutureCallback(future));
      }
    }
  }

  /**
   * to dispatch a notification within the context of a current session. If this method is called within a
   * {@link ClientRunContext} containing the same session as the addressed session (method argument) the notification
   * will be dispatched sync in the current {@link RunContext}.
   *
   * @param session
   *          the session describes the runcontext in which the notification should be processed.
   * @param notification
   *          the notification to process.
   */
  public void dispatch(IClientSession session, Serializable notification) {
    P_DispatchRunnable dispatchJob = new P_DispatchRunnable(notification);
    ISession currentSession = ISession.CURRENT.get();
    // sync dispatch if session is equal
    if (session == currentSession) {
      ClientRunContexts.copyCurrent().run(dispatchJob, BEANS.get(RuntimeExceptionTranslator.class));
    }
    else {
      IFuture<Void> future = ClientJobs.schedule(dispatchJob, ClientJobs.newInput(ClientRunContexts.empty().session(session, true)));
      synchronized (m_notificationFutures) {
        m_notificationFutures.add(future);
        future.whenDone(new P_NotificationFutureCallback(future));
      }
    }
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

  /**
   * The runnable is executed in a {@link ClientRunContext} running under a user session. All handlers with a message
   * type assignable from the notification type will be called to process the notification.
   */
  private class P_DispatchRunnable implements IRunnable {

    private final Serializable m_notification;

    public P_DispatchRunnable(Serializable notification) {
      m_notification = notification;
    }

    @Override
    public void run() throws Exception {
      NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
      reg.notifyHandlers(m_notification);
    }
  }

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
