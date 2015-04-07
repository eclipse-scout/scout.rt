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
package org.eclipse.scout.rt.server;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;

/**
 * Default server-side {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanManager()}
 */
@Replace
public class ServerBeanDecorationFactory extends SimpleBeanDecorationFactory {

  @Override
  public <T> IBeanInterceptor<T> decorate(IBean<T> bean, Class<T> queryType) {
    if (bean.getBeanAnnotation(Server.class) != null) {
      return decorateWithServerSessionCheck(bean, queryType);
    }
    return super.decorate(bean, queryType);
  }

  protected <T> IBeanInterceptor<T> decorateWithServerSessionCheck(IBean<T> bean, Class<T> queryType) {
    //TODO imo add context check
    return null;
  }

}
