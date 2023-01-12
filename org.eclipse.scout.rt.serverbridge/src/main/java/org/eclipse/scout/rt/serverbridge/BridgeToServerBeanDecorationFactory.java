/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.serverbridge;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Bean decoration factory used in applications running the server and client part on the same Scout bean manager.<br>
 * Instead of tunneling backend calls using serialization it directly bridges to the server.<br>
 * All bean having the {@link TunnelToServer} annotation are executed in a server context.
 *
 * @see BridgeToServerBeanDecorator
 * @since 5.2
 */
@Replace
public class BridgeToServerBeanDecorationFactory extends SimpleBeanDecorationFactory {
  @Override
  public <T> IBeanDecorator<T> decorate(IBean<T> bean, Class<? extends T> queryType) {
    IBeanDecorator<T> decorator = super.decorate(bean, queryType);
    if (bean.hasAnnotation(TunnelToServer.class)) {
      decorator = new BridgeToServerBeanDecorator<>(decorator);
    }
    return decorator;
  }
}
