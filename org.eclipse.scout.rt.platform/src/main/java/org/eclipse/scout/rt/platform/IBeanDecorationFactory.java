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
