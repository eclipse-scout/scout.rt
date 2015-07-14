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

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.SharedConfigProperties.NotificationSubjectProperty;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 *
 */
@ApplicationScoped
@CreateImmediately
public class ClientNotificationPoller {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationPoller.class);
  private IFuture<Void> m_pollerFuture;

  @PostConstruct
  public void start() {
    // TODO aho guard to start only once
    if (BEANS.get(IServiceTunnel.class).isActive()) {
      P_NotificationPollJob pollJob = new P_NotificationPollJob();
      m_pollerFuture = Jobs.schedule(pollJob,
          Jobs.newInput(ClientRunContexts.copyCurrent()
              .withSubject(BEANS.get(NotificationSubjectProperty.class).getValue())
              .withUserAgent(UserAgent.createDefault())
              .withSession(null, false)));
    }
    else {
      LOG.debug("Starting without notifications due to no proxy service is available");
    }
  }

  public void stop() {
    if (m_pollerFuture != null) {
      m_pollerFuture.cancel(true);
    }
  }

  protected void handleMessagesReceived(List<ClientNotificationMessage> notifications) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("CLIENT NOTIFICATION returned with {} notifications ({}).", notifications.size(), notifications);
    }
    // process notifications
    if (!notifications.isEmpty()) {
      BEANS.get(ClientNotificationDispatcher.class).dispatchNotifications(notifications, new IFilter<ClientNotificationMessage>() {
        @Override
        public boolean accept(ClientNotificationMessage message) {
          return !CompareUtility.equals(message.getAddress().getExcludedNodeId(), IClientSessionRegistry.NOTIFICATION_NODE_ID);
        }
      });
    }
  }

  private class P_NotificationPollJob implements IRunnable {
    @Override
    public void run() throws InterruptedException {
      while (!RunMonitor.CURRENT.get().isCancelled()) {
        try {
          IClientNotificationService svc = BEANS.get(IClientNotificationService.class);
          handleMessagesReceived(svc.getNotifications(IClientSessionRegistry.NOTIFICATION_NODE_ID));
        }
        catch (Exception e) {
          //server error?
          LOG.error("Error receiving client notifications", e);
          Thread.sleep(10000);
        }
      }
    }
  }
}
