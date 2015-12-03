/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
