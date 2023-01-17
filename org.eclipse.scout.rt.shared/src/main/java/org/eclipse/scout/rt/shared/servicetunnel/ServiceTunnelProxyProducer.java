/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.interceptor.DecoratingProxy;
import org.eclipse.scout.rt.platform.interceptor.IInstanceInvocationHandler;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthias Villiger
 */
public class ServiceTunnelProxyProducer<T> implements IBeanInstanceProducer<T>, IInstanceInvocationHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelProxyProducer.class);

  private final DecoratingProxy<T> m_proxy;

  private final Class<?> m_interfaceClass;

  public ServiceTunnelProxyProducer(Class<?> interfaceClass) {
    m_interfaceClass = interfaceClass;
    m_proxy = DecoratingProxy.newInstance(this, interfaceClass);
  }

  @Override
  public T produce(IBean<T> bean) {
    return m_proxy.getProxy();
  }

  @Override
  public Object invoke(T instance /*will always be null*/, Method method, Object[] args) throws Throwable {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Tunnel call to {}.{}({})", getInterfaceClass(), method.getName(), VerboseUtility.dumpObjects(args));
    }

    return BEANS.get(IServiceTunnel.class).invokeService(getInterfaceClass(), method, args);
  }

  protected Class<?> getInterfaceClass() {
    return m_interfaceClass;
  }
}
