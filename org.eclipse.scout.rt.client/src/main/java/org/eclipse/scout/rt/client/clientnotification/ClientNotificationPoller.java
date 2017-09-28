/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.clientnotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.INode;
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
          .withRunContext(ClientRunContexts.empty()
              .withSubject(BEANS.get(NotificationSubjectProperty.class).getValue())
              .withUserAgent(UserAgents.createDefault())
              .withSession(null, false))
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

    LOG.debug("Stopping client notification poller.");
    m_pollerFuture.cancel(true);
    m_pollerFuture = null;
  }

  protected static void handleMessagesReceived(List<ClientNotificationMessage> notifications) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("CLIENT NOTIFICATION returned with {} notifications ({}).", notifications.size(), notifications);
    }
    // process notifications
    if (!notifications.isEmpty()) {
      BEANS.get(ClientNotificationDispatcher.class).dispatchNotifications(notifications);
    }
  }

  private static final class P_NotificationPoller implements IRunnable {

    @Override
    public void run() {
      while (!RunMonitor.CURRENT.get().isCancelled()) {
        try {
          handleMessagesReceived(BEANS.get(IClientNotificationService.class).getNotifications(INode.ID));
        }
        catch (ThreadInterruptedError | FutureCancelledError e) {
          LOG.debug("Client notification polling has been interrupted.", e);
        }
        catch (RuntimeException e) {
          LOG.error("Error receiving client notifications", e);
          SleepUtil.sleepSafe(10, TimeUnit.SECONDS); // sleep some time before connecting anew
        }
      }
      LOG.debug("Client notification polling has ended because the job was cancelled.");
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
        BEANS.get(ClientNotificationPoller.class).stop();
      }
    }
  }
}
