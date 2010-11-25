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
package org.eclipse.scout.rt.client.servicetunnel;

import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

public interface IServiceTunnel {

  /**
   * Invoke a remote service through a service tunnel<br>
   * The argument array may contain IHolder values which are updated as OUT
   * parameters when the backend call has completed flags are custom flags not
   * used by the framework itself
   */
  Object invokeService(Class<?> serviceInterfaceClass, Method operation, Object[] args) throws ProcessingException;

  URL getServerURL();

  void setServerURL(URL url);

  /**
   * see {@link #setClientNotificationPollInterval(long)} default is -1L (turned
   * off), when activated a value of 2000L is recommended.
   */
  long getClientNotificationPollInterval();

  /**
   * Set the intervall to automatically read {@link IClientNotification}s from
   * the server. A negative value disables polling. Note: {@link IClientNotification}s are also recevied on every tunnel
   * reponse from
   * the server.
   */
  void setClientNotificationPollInterval(long intervallMillis);

  /**
   * see {@link #setAnalyzeNetworkLatency(boolean)} default is true
   */
  boolean isAnalyzeNetworkLatency();

  /**
   * If true the client notification polling process analyzes network latency to
   * optimize the poll interval in order to save the network. for Experts:
   * constant N is defined as: N=10 Assertion is: pollInterval >
   * N*networkLatency Example: the initial pollInterval is 2000ms and the moving
   * average of the networkLatency reaches 700ms, then the used polling interval
   * will be max(2000ms,N*700ms) -> 7000ms
   */
  void setAnalyzeNetworkLatency(boolean b);

}
