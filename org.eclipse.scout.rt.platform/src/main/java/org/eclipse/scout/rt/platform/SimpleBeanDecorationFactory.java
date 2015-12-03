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
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;

/**
 * Default simple {@link IBeanDecorationFactory} used in {@link IPlatform#getBeanManager()}
 */
public class SimpleBeanDecorationFactory implements IBeanDecorationFactory {

  @Override
  public <T> IBeanDecorator<T> decorate(IBean<T> bean, Class<? extends T> queryType) {
    return null;
  }

}
