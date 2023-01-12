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
 * Used in {@link IBeanManager}
 * <p>
 * Knows how to decorate an object instance of a {@link IBean} wrapped with a runtime context (multiple decorators)
 * <p>
 *
 * @see IBeanDecorator
 * @since 5.1
 */
@Bean
public interface IBeanDecorationFactory {
  /**
   * @return the decorated instance, may be <code>null</code>, if no decoration is needed
   */
  <T> IBeanDecorator<T> decorate(IBean<T> bean, Class<? extends T> queryType);
}
