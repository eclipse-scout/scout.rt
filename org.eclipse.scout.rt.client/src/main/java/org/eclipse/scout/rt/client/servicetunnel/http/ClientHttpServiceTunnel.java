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
package org.eclipse.scout.rt.client.servicetunnel.http;

import java.net.URL;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.client.clientnotification.IClientSessionRegistry;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.services.common.offline.IOfflineDispatcherService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.http.AbstractHttpServiceTunnel;

/**
 * Client-side tunnel used to invoke a service through HTTP. This class re-defines methods of it's super class
 * since the internal class does not belong to the public API.
 */
public class ClientHttpServiceTunnel extends AbstractHttpServiceTunnel implements IClientServiceTunnel {

//  private IFuture<Void> m_pollingJob;
//  private long m_pollInterval = -1L;
  private boolean m_analyzeNetworkLatency = true;

  public ClientHttpServiceTunnel() {
    super();
  }

  public ClientHttpServiceTunnel(URL url) {
    super(url);
  }

//  @Override
//  public long getClientNotificationPollInterval() {
//    return m_pollInterval;
//  }
//
//  @Override
//  public void setClientNotificationPollInterval(long intervallMillis) {
//    long oldInterval = m_pollInterval;
//    m_pollInterval = intervallMillis;
//    if (m_pollInterval != oldInterval) {
//      updatePollingJobInternal();
//    }
//  }

  @Override
  public boolean isAnalyzeNetworkLatency() {
    return m_analyzeNetworkLatency;
  }

  @Override
  public void setAnalyzeNetworkLatency(boolean b) {
    m_analyzeNetworkLatency = b;
  }

  @Override
  protected void beforeTunnel(ServiceTunnelRequest serviceRequest) {
    ISession session = IClientSession.CURRENT.get();
    if (session != null) {
      serviceRequest.setSessionId(session.getId());
    }
    serviceRequest.setClientNotificationNodeId(IClientSessionRegistry.NOTIFICATION_NODE_ID);
    // TODO piggyback notifications
//    IClientNotificationConsumerService cns = BEANS.get(IClientNotificationConsumerService.class);
//    if (call instanceof ServiceTunnelRequest && cns != null) {
//     ((ServiceTunnelRequest) call).setConsumedNotifications(cns.getConsumedNotificationIds(getSession()));
//    }
  }

  @Override
  protected void afterTunnel(long t0, IServiceTunnelResponse serviceResponse) {
    if (isAnalyzeNetworkLatency()) {
      // performance analyzer
      IPerformanceAnalyzerService perf = BEANS.get(IPerformanceAnalyzerService.class);
      if (perf != null) {
        long totalMillis = (System.nanoTime() - t0) / 1000000L;
        Long execMillis = serviceResponse.getProcessingDuration();
        if (execMillis != null) {
          perf.addNetworkLatencySample(totalMillis - execMillis);
          perf.addServerExecutionTimeSample(execMillis);
        }
        else {
          perf.addNetworkLatencySample(totalMillis);
        }
      }
    }

    // TODO [aho] piggyback notifications
    ClientNotificationDispatcher notificationDispatcher = BEANS.get(ClientNotificationDispatcher.class);
    notificationDispatcher.dispatchNotifications(serviceResponse.getNotifications());
//    // client notification handler
//    IClientNotificationConsumerService cns = BEANS.get(IClientNotificationConsumerService.class);
//    if (cns != null) {
//      cns.dispatchClientNotifications(response.getClientNotifications(), getSession());
//    }
  }

//  private synchronized void updatePollingJobInternal() {
//    long p = getClientNotificationPollInterval();
//    if (p > 0) {
//      if (m_pollingJob != null) {
//        // cancel the old
//        m_pollingJob.cancel(true);
//      }
//      ClientRunContext runContext = ClientRunContexts.copyCurrent().session(getSession());
//      m_pollingJob = ClientJobs.scheduleWithFixedDelay(new ClientNotificationPollingJob(), p, p, TimeUnit.MILLISECONDS, ClientJobs.newInput(runContext).name("Client notification fetcher"));
//    }
//    else {
//      if (m_pollingJob != null) {
//        m_pollingJob.cancel(true);
//        m_pollingJob = null;
//      }
//    }
//  }

  @Override
  protected IServiceTunnelResponse tunnel(ServiceTunnelRequest serviceRequest) {
    if (OfflineState.isOfflineInCurrentThread()) {
      return tunnelOffline(serviceRequest);
    }
    else {
      return tunnelOnline(serviceRequest);
    }
  }

  protected IServiceTunnelResponse tunnelOnline(final ServiceTunnelRequest serviceRequest) {
    return super.tunnel(serviceRequest);
  }

  /**
   * Default for offline handling
   */
  protected IServiceTunnelResponse tunnelOffline(final ServiceTunnelRequest serviceRequest) {
    IClientSession clientSession = ClientSessionProvider.currentSession();
    if (clientSession != null && clientSession.getOfflineSubject() != null) {
      Object response = Subject.doAs(clientSession.getOfflineSubject(), new PrivilegedAction<IServiceTunnelResponse>() {
        @Override
        public IServiceTunnelResponse run() {
          return BEANS.get(IOfflineDispatcherService.class).dispatch(serviceRequest);
        }
      });
      return (IServiceTunnelResponse) response;
    }
    else {
      return BEANS.get(IOfflineDispatcherService.class).dispatch(serviceRequest);
    }
  }

  @Override
  protected RunContext createCurrentRunContext() {
    return ClientRunContexts.copyCurrent();
  }
}
