/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
