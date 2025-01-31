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

import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;

/**
 * Interface for a client side service tunnel used to invoke a service.
 */
public interface IClientServiceTunnel extends IServiceTunnel {

  /**
   * see {@link #setAnalyzeNetworkLatency(boolean)} default is true
   */
  boolean isAnalyzeNetworkLatency();

  /**
   * If true the client notification polling process analyzes network latency to optimize the poll interval in order to
   * save the network. for Experts: constant N is defined as: N=10 Assertion is: pollInterval &gt; N*networkLatency
   * Example: the initial pollInterval is 2000ms and the moving average of the networkLatency reaches 700ms, then the
   * used polling interval will be max(2000ms,N*700ms) -&gt; 7000ms
   */
  void setAnalyzeNetworkLatency(boolean b);
}
