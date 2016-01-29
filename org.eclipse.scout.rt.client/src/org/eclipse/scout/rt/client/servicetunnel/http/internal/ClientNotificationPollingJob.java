/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.service.SERVICES;

public class ClientNotificationPollingJob extends ClientAsyncJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationPollingJob.class);

  private long m_pollInterval;
  private boolean m_analyzeNetworkLatency;
  private volatile boolean m_disposed;

  public ClientNotificationPollingJob(IClientSession session, long pollInterval, boolean analyzeNetworkLatency) {
    super("Client notification fetcher", session, true);
    m_disposed = false;
    updatePollingValues(pollInterval, analyzeNetworkLatency);
  }

  public void updatePollingValues(long pollInterval, boolean analyzeNetworkLatency) {
    m_pollInterval = Math.max(1000L, pollInterval);
    m_analyzeNetworkLatency = analyzeNetworkLatency;
  }

  public void dispose() {
    m_disposed = true;
  }

  public boolean isDisposed() {
    return m_disposed;
  }

  @Override
  protected IStatus runStatus(IProgressMonitor monitor) {
    if (isDisposed() || monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    IPingService pingService = SERVICES.getService(IPingService.class);
    try {
      // side-effect of every service call (whether ping or any other) is to get
      // client notifications
      pingService.ping("GetClientNotifications");
    }
    catch (Throwable t) {
      if (LOG.isInfoEnabled()) {
        LOG.info("polling", t);
      }
    }
    if (isDisposed() || monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    // re-schedule
    long netLatency = 0L;
    IPerformanceAnalyzerService perf = SERVICES.getService(IPerformanceAnalyzerService.class);
    if (perf != null) {
      netLatency = perf.getNetworkLatency();
    }
    long sleepInterval = m_analyzeNetworkLatency ? Math.max(m_pollInterval, 10 * netLatency) : m_pollInterval;
    schedule(sleepInterval);
    return Status.OK_STATUS;
  }
}
