/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;

/**
 * Invocation handler that uses the {@link IServiceTunnel} available in the current {@link IClientSession}.
 */
public class ClientServiceTunnelInvocationHandler implements InvocationHandler {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientServiceTunnelInvocationHandler.class);

  private final Class<?> m_serviceInterfaceClass;

  public ClientServiceTunnelInvocationHandler(Class<?> interfaceClass) {
    m_serviceInterfaceClass = interfaceClass;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.isAssignableFrom(method.getDeclaringClass())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Soap call to " + m_serviceInterfaceClass.getName() + "." + method.getName() + "(" + VerboseUtility.dumpObjects(args) + ")");
      }
      IClientSession session = ClientJob.getCurrentSession();
      return session.getServiceTunnel().invokeService(m_serviceInterfaceClass, method, args);
    }
    else {
      return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    }
  }

}
