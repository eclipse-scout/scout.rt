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

import java.lang.reflect.InvocationHandler;

import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;

/**
 * @Deprecated: use {@link org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility} instead
 *              To be removed with the K-Release
 */
@Deprecated
public final class ServiceTunnelUtility {

  private ServiceTunnelUtility() {
  }

  @SuppressWarnings("deprecation")
  public static <T> T createProxy(Class<T> serviceInterfaceClass, IServiceTunnel tunnel) {
    return org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility.createProxy(serviceInterfaceClass, tunnel);
  }

  public static <T> T createProxy(Class<T> serviceInterfaceClass, IClientServiceTunnel tunnel) {
    return org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility.createProxy(serviceInterfaceClass, tunnel);
  }

  public static <T> T createProxy(Class<T> serviceInterfaceClass, InvocationHandler handler) {
    return org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility.createProxy(serviceInterfaceClass, handler);
  }
}
