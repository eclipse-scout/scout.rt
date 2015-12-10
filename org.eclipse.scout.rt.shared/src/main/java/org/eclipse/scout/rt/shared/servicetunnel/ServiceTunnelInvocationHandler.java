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
import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java proxy handler through a service tunnel.
 */
public class ServiceTunnelInvocationHandler implements InvocationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelInvocationHandler.class);

  private final Class<?> m_serviceInterfaceClass;

  public ServiceTunnelInvocationHandler(Class<?> serviceInterfaceClass) {
    if (serviceInterfaceClass == null) {
      throw new IllegalArgumentException("serviceInterfaceClass must not be null");
    }
    m_serviceInterfaceClass = serviceInterfaceClass;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // only proxy methods that are on the IService interface
    if (Object.class.isAssignableFrom(method.getDeclaringClass())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Soap call to {}.{}({})", m_serviceInterfaceClass.getName(), method.getName(), VerboseUtility.dumpObjects(args));
      }
      return BEANS.get(IServiceTunnel.class).invokeService(m_serviceInterfaceClass, method, args);
    }
    else {
      try {
        return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
      }
      catch (Throwable t) {
        throw BEANS.get(ThrowableTranslator.class).translate(t);
      }
    }
  }

}
