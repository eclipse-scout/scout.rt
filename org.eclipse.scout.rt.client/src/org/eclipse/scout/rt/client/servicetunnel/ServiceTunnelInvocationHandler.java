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
import java.lang.reflect.Method;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * java proxy handler through a service tunnel
 */
public class ServiceTunnelInvocationHandler implements InvocationHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceTunnelInvocationHandler.class);

  private final Class<?> m_serviceInterfaceClass;
  private final IServiceTunnel m_tunnel;

  public ServiceTunnelInvocationHandler(Class<?> serviceInterfaceClass, IServiceTunnel tunnel) {
    if (serviceInterfaceClass == null) {
      throw new IllegalArgumentException("serviceInterfaceClass must not be null");
    }
    if (tunnel == null) {
      throw new IllegalArgumentException("tunnel must not be null");
    }
    m_serviceInterfaceClass = serviceInterfaceClass;
    m_tunnel = tunnel;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // only proxy methods that are on the IService interface
    if (Object.class.isAssignableFrom(method.getDeclaringClass())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Soap call to " + m_serviceInterfaceClass.getName() + "." + method.getName() + "(" + VerboseUtility.dumpObjects(args) + ")");
      }
      return m_tunnel.invokeService(m_serviceInterfaceClass, method, args);
    }
    else {
      return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    }
  }

}
