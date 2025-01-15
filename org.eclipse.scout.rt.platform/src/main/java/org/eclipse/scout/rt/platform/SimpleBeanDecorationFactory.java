/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
