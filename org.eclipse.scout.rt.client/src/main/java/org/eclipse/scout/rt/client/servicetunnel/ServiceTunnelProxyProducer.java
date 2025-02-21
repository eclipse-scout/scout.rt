/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel;

import static org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions.ID_SIGNATURE_PROP;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.interceptor.DecoratingProxy;
import org.eclipse.scout.rt.platform.interceptor.IInstanceInvocationHandler;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthias Villiger
 */
public class ServiceTunnelProxyProducer<T> implements IBeanInstanceProducer<T>, IInstanceInvocationHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceTunnelProxyProducer.class);

  private final DecoratingProxy<T> m_proxy;

  private final Class<?> m_interfaceClass;
  private final ServiceTunnelOptions m_options;

  public ServiceTunnelProxyProducer(Class<?> interfaceClass) {
    this(interfaceClass, null);
  }

  public ServiceTunnelProxyProducer(Class<?> interfaceClass, ServiceTunnelOptions options) {
    m_interfaceClass = interfaceClass;
    m_options = ObjectUtility.nvlOpt(options, ServiceTunnelOptions::create);
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

    Callable<Object> invokeService = () -> BEANS.get(HttpServiceTunnel.class).invokeService(getInterfaceClass(), method, args);

    // check the idSignature flag
    if (getOptions().isIdSignature()) {
      // add run context property
      return RunContexts.copyCurrent()
          .withProperty(ID_SIGNATURE_PROP, true)
          .call(invokeService);
    }

    return invokeService.call();
  }

  protected Class<?> getInterfaceClass() {
    return m_interfaceClass;
  }

  public ServiceTunnelOptions getOptions() {
    return m_options;
  }
}
