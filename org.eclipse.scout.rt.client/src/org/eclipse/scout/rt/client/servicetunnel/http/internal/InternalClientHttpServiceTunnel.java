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

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.servicetunnel.http.internal.AbstractInternalHttpServiceTunnel;
import org.eclipse.scout.rt.servicetunnel.http.internal.HttpBackgroundExecutable;
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
public class InternalClientHttpServiceTunnel extends AbstractInternalHttpServiceTunnel<IClientSession> implements IClientServiceTunnel {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InternalClientHttpServiceTunnel.class);

  private ClientNotificationPollingJob m_pollingJob;

  private final Object m_pollingJobLock = new Object();

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
    m_pollInterval = intervallMillis;
    updatePollingJobInternal();
  }

  @Override
  public boolean isAnalyzeNetworkLatency() {
    return m_analyzeNetworkLatency;
  }

  @Override
  public void setAnalyzeNetworkLatency(boolean b) {
    m_analyzeNetworkLatency = b;
    updatePollingJobInternal();
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
    // client notification handler
    IClientNotificationConsumerService cns = SERVICES.getService(IClientNotificationConsumerService.class);
    if (cns != null) {
      cns.dispatchClientNotifications(response.getClientNotifications(), getSession());
    }
  }

  private void updatePollingJobInternal() {
    synchronized (m_pollingJobLock) {
      long p = getClientNotificationPollInterval();
      boolean b = isAnalyzeNetworkLatency();
      if (p > 0) {
        if (m_pollingJob == null) {
          m_pollingJob = new ClientNotificationPollingJob(getSession(), p, b);
          m_pollingJob.schedule();
        }
        else {
          m_pollingJob.updatePollingValues(p, b);
        }
      }
      else {
        if (m_pollingJob != null) {
          m_pollingJob.dispose();
          m_pollingJob = null;
        }
      }
    }
  }

  @Override
  protected IServiceTunnelResponse tunnel(IServiceTunnelRequest call) {
    boolean offline = OfflineState.isOfflineInCurrentThread();
    //
    if (offline) {
      return tunnelOffline(call);
    }
    else {
      return tunnelOnline(call);
    }
  }

  protected IServiceTunnelResponse tunnelOnline(final IServiceTunnelRequest req) {
    if (ClientJob.isCurrentJobCanceled()) {
      return new ServiceTunnelResponse(null, null, new InterruptedException(ScoutTexts.get("UserInterrupted")));
    }
    return super.tunnel(req);
  }

  /**
   * Default for offline handling
   */
  protected IServiceTunnelResponse tunnelOffline(final IServiceTunnelRequest call) {
    final IProgressMonitor monitor;
    Job job = Job.getJobManager().currentJob();
    if (job instanceof ClientJob) {
      monitor = ((ClientJob) job).getMonitor();
    }
    else {
      monitor = new NullProgressMonitor();
    }
    IClientSession clientSession = ClientSyncJob.getCurrentSession();
    if (clientSession != null && clientSession.getOfflineSubject() != null) {
      Object response = Subject.doAs(clientSession.getOfflineSubject(), new PrivilegedAction<IServiceTunnelResponse>() {
        @Override
        public IServiceTunnelResponse run() {
          return SERVICES.getService(IOfflineDispatcherService.class).dispatch(call, monitor);
        }
      });
      return (IServiceTunnelResponse) response;
    }
    else {
      return SERVICES.getService(IOfflineDispatcherService.class).dispatch(call, monitor);
    }
  }

  @Override
  protected void decorateBackgroundJob(IServiceTunnelRequest call, Job backgroundJob) {
    backgroundJob.setUser(false);
    backgroundJob.setSystem(true);
  }

  @Override
  protected JobEx createHttpBackgroundJob(String name, final HttpBackgroundExecutable executable) {
    return new ClientAsyncJob(name, getSession(), true) {
      @Override
      protected IStatus runStatus(IProgressMonitor monitor) {
        return executable.run(monitor);
      }
    };
  }

}
