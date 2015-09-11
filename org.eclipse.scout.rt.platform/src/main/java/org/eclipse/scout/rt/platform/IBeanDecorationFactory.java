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
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;

/**
 * Used in {@link IBeanManager}
 * <p>
 * Knows how to decorate an object instance of a {@link IBean} wrapped with a runtime context (multiple interceptors)
 * using all the known {@link BeanInvocationHint} annotations on the beans
 * <p>
 * see {@link IBeanInstanceProducer}
 * 
 * @since 5.1
 */
@Bean
public interface IBeanDecorationFactory {
  /**
   * @return the decorated instance
   */
  <T> IBeanInterceptor<T> decorate(IBean<T> bean, Class<? extends T> queryType);
}
