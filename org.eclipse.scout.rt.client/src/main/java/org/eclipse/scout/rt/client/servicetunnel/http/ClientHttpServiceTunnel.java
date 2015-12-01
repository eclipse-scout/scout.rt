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
import java.util.List;

import org.eclipse.scout.rt.client.IClientNode;
import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.http.AbstractHttpServiceTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side tunnel used to invoke a service through HTTP. This class re-defines methods of it's super class since the
 * internal class does not belong to the public API.
 */
public class ClientHttpServiceTunnel extends AbstractHttpServiceTunnel implements IClientServiceTunnel {

  private static final Logger LOG = LoggerFactory.getLogger(ClientHttpServiceTunnel.class);

  private boolean m_analyzeNetworkLatency = true;

  public ClientHttpServiceTunnel() {
    super();
  }

  public ClientHttpServiceTunnel(URL url) {
    super(url);
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
  protected void beforeTunnel(ServiceTunnelRequest serviceRequest) {
    ISession session = ISession.CURRENT.get();
    if (session != null) {
      serviceRequest.setSessionId(session.getId());
    }
    else {
      // use this client's node-id for session less communication
      serviceRequest.setSessionId(IClientNode.ID);
    }
    serviceRequest.setClientNodeId(IClientNode.ID);
  }

  @Override
  protected void afterTunnel(long t0, ServiceTunnelResponse serviceResponse) {
    if (isAnalyzeNetworkLatency()) {
      // performance analyzer
      IPerformanceAnalyzerService perf = BEANS.opt(IPerformanceAnalyzerService.class);
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

    // process piggyback client notifications.
    try {
      dispatchClientNotifications(serviceResponse.getNotifications());
    }
    catch (RuntimeException e) {
      LOG.error("Error during processing piggyback client notifictions.", e);
    }
  }

  /**
   * dispatch notifications in a client job and ensure to wait for dispatched notifications
   *
   * @param notifications
   *          the notifications to dispatch
   */
  protected void dispatchClientNotifications(final List<ClientNotificationMessage> notifications) {
    if (CollectionUtility.isEmpty(notifications)) {
      return;
    }
    final IBlockingCondition cond = Jobs.getJobManager().createBlockingCondition("Suspend request processing thread during client notification handling.", true);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        ClientNotificationDispatcher notificationDispatcher = BEANS.get(ClientNotificationDispatcher.class);
        notificationDispatcher.dispatchNotifications(notifications);
      }
    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent()))
        .whenDone(new IDoneHandler<Void>() {

          @Override
          public void onDone(DoneEvent<Void> event) {
            cond.setBlocking(false);
          }
        }, null);
    cond.waitFor();
  }

  @Override
  protected RunContext createCurrentRunContext() {
    return ClientRunContexts.copyCurrent();
  }
}
