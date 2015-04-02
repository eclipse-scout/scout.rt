/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import java.net.URL;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IProgressMonitor;
import org.eclipse.scout.rt.platform.job.JobExecutionException;
import org.eclipse.scout.rt.servicetunnel.http.AbstractHttpServiceTunnel;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.offline.IOfflineDispatcherService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.service.SERVICES;

/**
 * Non-public implementation of a client-side tunnel used to invoke a service through HTTP.
 *
 * @author awe
 */
public class InternalClientHttpServiceTunnel extends AbstractHttpServiceTunnel<IClientSession> implements IClientServiceTunnel {

  private IFuture<Void> m_pollingJob;
  private long m_pollInterval = -1L;
  private boolean m_analyzeNetworkLatency = true;

  public InternalClientHttpServiceTunnel(IClientSession session, URL url) {
    this(session, url, null);
  }

  /**
   * @param url
   * @param version
   *          the version that is sent down to the server with every request.
   *          This allows the server to check client request and refuse old
   *          clients. Check the servers HttpProxyHandlerServlet init-parameter
   *          (example: min-version="0.0.0") If the version parameter is null,
   *          the product bundle (for example com.myapp.ui.swing) version is
   *          used
   */
  public InternalClientHttpServiceTunnel(IClientSession session, URL url, String version) {
    super(session, url, version);
  }

  @Override
  public long getClientNotificationPollInterval() {
    return m_pollInterval;
  }

  @Override
  public void setClientNotificationPollInterval(long intervallMillis) {
    long oldInterval = m_pollInterval;
    m_pollInterval = intervallMillis;
    if (m_pollInterval != oldInterval) {
      updatePollingJobInternal();
    }
  }

  @Override
  public boolean isAnalyzeNetworkLatency() {
    return m_analyzeNetworkLatency;
  }

  @Override
  public void setAnalyzeNetworkLatency(boolean b) {
    m_analyzeNetworkLatency = b;
  }

  @Override
  protected void decorateServiceRequest(IServiceTunnelRequest call) {
    IClientNotificationConsumerService cns = SERVICES.getService(IClientNotificationConsumerService.class);
    if (call instanceof ServiceTunnelRequest && cns != null) {
      ((ServiceTunnelRequest) call).setConsumedNotifications(cns.getConsumedNotificationIds(getSession()));
    }
  }

  @Override
  protected void onInvokeService(long t0, IServiceTunnelResponse response) {
    if (isAnalyzeNetworkLatency()) {
      // performance analyzer
      IPerformanceAnalyzerService perf = SERVICES.getService(IPerformanceAnalyzerService.class);
      if (perf != null) {
        long totalMillis = (System.nanoTime() - t0) / 1000000L;
        Long execMillis = response.getProcessingDuration();
        if (execMillis != null) {
          perf.addNetworkLatencySample(totalMillis - execMillis);
          perf.addServerExecutionTimeSample(execMillis);
        }
        else {
          perf.addNetworkLatencySample(totalMillis);
        }
      }
    }

    // client notification handler
    IClientNotificationConsumerService cns = SERVICES.getService(IClientNotificationConsumerService.class);
    if (cns != null) {
      cns.dispatchClientNotifications(response.getClientNotifications(), getSession());
    }
  }

  private synchronized void updatePollingJobInternal() {
    long p = getClientNotificationPollInterval();
    if (p > 0) {
      if (m_pollingJob != null) {
        // cancel the old
        m_pollingJob.cancel(true);
      }
      ClientRunContext runContext = ClientRunContexts.copyCurrent().session(getSession());
      m_pollingJob = ClientJobs.scheduleWithFixedDelay(new ClientNotificationPollingJob(), p, p, TimeUnit.MILLISECONDS, ClientJobs.newInput(runContext).name("Client notification fetcher"));
    }
    else {
      if (m_pollingJob != null) {
        m_pollingJob.cancel(true);
        m_pollingJob = null;
      }
    }
  }

  @Override
  protected IServiceTunnelResponse tunnel(IServiceTunnelRequest call) throws JobExecutionException {
    boolean offline = OfflineState.isOfflineInCurrentThread();
    //
    if (offline) {
      return tunnelOffline(call);
    }
    else {
      return tunnelOnline(call);
    }
  }

  protected IServiceTunnelResponse tunnelOnline(final IServiceTunnelRequest req) throws JobExecutionException {
    if (IProgressMonitor.CURRENT.get().isCancelled()) {
      return new ServiceTunnelResponse(null, null, new InterruptedException(ScoutTexts.get("UserInterrupted")));
    }
    return super.tunnel(req);
  }

  /**
   * Default for offline handling
   */
  protected IServiceTunnelResponse tunnelOffline(final IServiceTunnelRequest call) {
    IClientSession clientSession = ClientSessionProvider.currentSession();
    if (clientSession != null && clientSession.getOfflineSubject() != null) {
      Object response = Subject.doAs(clientSession.getOfflineSubject(), new PrivilegedAction<IServiceTunnelResponse>() {
        @Override
        public IServiceTunnelResponse run() {
          return SERVICES.getService(IOfflineDispatcherService.class).dispatch(call);
        }
      });
      return (IServiceTunnelResponse) response;
    }
    else {
      return SERVICES.getService(IOfflineDispatcherService.class).dispatch(call);
    }
  }

  @Override
  protected IFuture<?> schedule(IRunnable runnable, IServiceTunnelRequest req) throws JobExecutionException {
    return ClientJobs.schedule(runnable, ClientJobs.newInput(ClientRunContexts.copyCurrent().session(getSession())));
  }
}
