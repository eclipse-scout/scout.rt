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

import java.lang.reflect.Method;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IBeanDecorator} that uses the {@link IServiceTunnel} available in the current {@link IClientSession}.
 */
public class TunnelToServerBeanDecorator<T> implements IBeanDecorator<T> {

  private static final Logger LOG = LoggerFactory.getLogger(TunnelToServerBeanDecorator.class);

  private final Class<?> m_serviceInterfaceClass;

  public TunnelToServerBeanDecorator(Class<?> interfaceClass) {
    m_serviceInterfaceClass = interfaceClass;
  }

  @Override
  public Object invoke(IBeanInvocationContext<T> context) {
    Method method = context.getTargetMethod();
    Object[] args = context.getTargetArgs();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Soap call to " + m_serviceInterfaceClass.getName() + "." + method.getName() + "(" + VerboseUtility.dumpObjects(args) + ")");
    }
    return BEANS.get(IServiceTunnel.class).invokeService(m_serviceInterfaceClass, method, args);
  }

}
