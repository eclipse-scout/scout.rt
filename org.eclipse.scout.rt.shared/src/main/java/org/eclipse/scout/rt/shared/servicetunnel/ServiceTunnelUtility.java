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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Creates a service proxy through a tunnel.
 */
public final class ServiceTunnelUtility {

  private ServiceTunnelUtility() {
  }

  public static <T> T createProxy(Class<T> serviceInterfaceClass) {
    return createProxy(serviceInterfaceClass, new ServiceTunnelInvocationHandler(serviceInterfaceClass));
  }

  @SuppressWarnings("unchecked")
  public static <T> T createProxy(Class<T> serviceInterfaceClass, InvocationHandler handler) {
    if (handler == null) {
      throw new IllegalArgumentException("handler is null");
    }
    return (T) Proxy.newProxyInstance(
        serviceInterfaceClass.getClassLoader(),
        new Class[]{serviceInterfaceClass},
        handler);
  }

}
