/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel.http;

import java.net.URL;
import java.util.List;

import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side tunnel used to invoke a service through HTTP. This class re-defines methods of it's super class since the
 * internal class does not belong to the public API.
 */
@Replace
public class ClientHttpServiceTunnel extends HttpServiceTunnel implements IClientServiceTunnel {

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
    final IBlockingCondition cond = Jobs.newBlockingCondition(true);
    Jobs.schedule(() -> {
      ClientNotificationDispatcher notificationDispatcher = BEANS.get(ClientNotificationDispatcher.class);
      notificationDispatcher.dispatchNotifications(notifications);
    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent()))
        .whenDone(event -> cond.setBlocking(false), null);
    cond.waitFor();
  }
}
