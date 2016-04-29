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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.internal.BeanProxyImplementor;
import org.eclipse.scout.rt.platform.internal.BeanImplementor;

/**
 * Creates a service proxy through a tunnel.
 */
public final class ServiceTunnelUtility {

  private ServiceTunnelUtility() {
  }

  public static <T> T createProxy(Class<T> serviceInterfaceClass) {
    ServiceTunnelProxyProducer<?> tunnelProxyProducer = new ServiceTunnelProxyProducer<>(serviceInterfaceClass);
    BeanMetaData metaData = new BeanMetaData(serviceInterfaceClass).withApplicationScoped(true).withProducer(tunnelProxyProducer);
    IBean<T> bean = new BeanImplementor<>(metaData);

    IBeanDecorationFactory factory = BEANS.opt(IBeanDecorationFactory.class);
    if (factory == null) {
      return bean.getInstance();
    }

    IBeanDecorator<T> decorator = factory.decorate(bean, serviceInterfaceClass);
    if (decorator == null) {
      return bean.getInstance();
    }

    return new BeanProxyImplementor<T>(bean, decorator, serviceInterfaceClass).getProxy();
  }
}
