/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.clientnotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.SharedConfigProperties.NotificationSubjectProperty;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@CreateImmediately
public class ClientNotificationPoller {

  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationPoller.class);

  private IFuture<Void> m_pollerFuture;

  @PostConstruct
  public void start() {
    // ensure the poller starts only once.
    Assertions.assertNull(m_pollerFuture);
    if (BEANS.get(IServiceTunnel.class).isActive()) {
      m_pollerFuture = Jobs.schedule(new P_NotificationPoller(), Jobs.newInput()
          .withRunContext(createRunContext())
          .withName(ClientNotificationPoller.class.getSimpleName()));
    }
    else {
      LOG.debug("Starting without notifications due to no proxy service is available");
    }
  }

  public void stop() {
    if (m_pollerFuture == null) {
      return;
    }

    LOG.debug("Stopping client notification poller [clientNodeId={}].", IIds.toString(NodeId.current()));
    m_pollerFuture.cancel(true);
    m_pollerFuture = null;
  }

  protected RunContext createRunContext() {
    return ClientRunContexts.empty()
        .withSubject(BEANS.get(NotificationSubjectProperty.class).getValue())
        .withUserAgent(UserAgents.createDefault())
        .withSession(null, false);
  }

  protected static void handleMessagesReceived(List<ClientNotificationMessage> notifications) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received {} notifications ({}) [clientNodeId={}].", notifications.size(), LOG.isTraceEnabled() ? notifications : "use level TRACE to see notifications", IIds.toString(NodeId.current()));
    }
    // process notifications
    if (!notifications.isEmpty()) {
      BEANS.get(ClientNotificationDispatcher.class).dispatchNotifications(notifications);
    }
    LOG.debug("Dispatched notifications [clientNodeId={}]", IIds.toString(NodeId.current()));
  }

  private static final class P_NotificationPoller implements IRunnable {

    @Override
    public void run() {
      final RunMonitor outerRunMonitor = RunMonitor.CURRENT.get();
      while (!outerRunMonitor.isCancelled()) {
        try {
          // use temporary new run monitor to avoid registering anonymous new cancellables with parent (current) run monitor
          // new local run monitor is registered with parent run monitor as cancellable, however it is unregistered from
          // parent after handling received messages in any case to avoid memory overusage
          final RunMonitor tempRunMonitor = BEANS.get(RunMonitor.class);
          RunContexts.copyCurrent()
              .withRunMonitor(tempRunMonitor)
              .withParentRunMonitor(outerRunMonitor)
              .run(() -> {
                try {
                  LOG.debug("Getting notifications from backend [clientNodeId={}]", IIds.toString(NodeId.current()));
                  handleMessagesReceived(BEANS.get(IClientNotificationService.class).getNotifications(NodeId.current()));
                }
                finally {
                  outerRunMonitor.unregisterCancellable(tempRunMonitor);
                }
              });
        }
        catch (ThreadInterruptedError | FutureCancelledError e) {
          LOG.debug("Client notification polling has been interrupted. [clientNodeId={}]", IIds.toString(NodeId.current()), e);
        }
        catch (RuntimeException e) {
          if (!(e instanceof PlatformException && ((PlatformException) e).isConsumed())) {
            LOG.error("Error receiving client notifications [clientNodeId={}]", IIds.toString(NodeId.current()), e);
          }
          SleepUtil.sleepSafe(10, TimeUnit.SECONDS); // sleep some time before connecting anew
        }
      }
      LOG.debug("Client notification polling has ended because the job was cancelled. [clientNodeId={}]", IIds.toString(NodeId.current()));
    }
  }

  /**
   * {@link IPlatformListener} to shutdown this {@link ClientNotificationPoller} upon platform shutdown.
   */
  @Order(-1000)
  public static final class ShutdownListener implements IPlatformListener {
    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        ClientNotificationPoller poller = BEANS.get(ClientNotificationPoller.class);
        poller.stop();
        poller.createRunContext().run(() -> BEANS.get(IClientNotificationService.class).unregisterNode(NodeId.current()));
      }
    }
  }
}
