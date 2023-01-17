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

import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.AnnotationFactory;
import org.eclipse.scout.rt.shared.servicetunnel.RegisterTunnelToServerPlatformListener;

/**
 * Instead of registering @TunnelToServer beans using interfaces we append the @TunnelToServer annotation to all beans
 * actually implementing a @TunnelToServer marked interface.
 *
 * @since 5.2
 */
@Replace
public class RegisterTunnelToServerBridgePlatformListener extends RegisterTunnelToServerPlatformListener {

  @Override
  protected boolean isEnabled() {
    return true;
  }

  @Override
  protected void registerTunnelToServerProxy(IBeanManager beanManager, Class<?> c) {
    List<? extends IBean<?>> beans = beanManager.getBeans(c);
    for (IBean<?> bean : beans) {
      BeanMetaData newMeta = new BeanMetaData(bean).withAnnotation(AnnotationFactory.createTunnelToServer());
      beanManager.unregisterBean(bean);
      beanManager.registerBean(newMeta);
    }
  }
}
