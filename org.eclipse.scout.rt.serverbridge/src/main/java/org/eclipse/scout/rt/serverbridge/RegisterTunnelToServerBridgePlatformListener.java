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
package org.eclipse.scout.rt.serverbridge;

import java.util.List;

import org.eclipse.scout.rt.client.RegisterTunnelToServerPlatformListener;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.AnnotationFactory;

/**
 * Instead of registering @TunnelToServer beans using interfaces we append the @TunnelToServer annotation to all beans
 * actually implementing a @TunnelToServer marked interface.
 * 
 * @since 5.2
 */
@Replace
public class RegisterTunnelToServerBridgePlatformListener extends RegisterTunnelToServerPlatformListener {
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
