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

import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * Used in {@link IBeanContext}
 * <p>
 * Knows how to create an object instance of a {@link IBean} wrapped with a runtime contex (multiple interceptors) using
 * all the known {@link BeanInvocationHint} annotations on the beans
 * <p>
 * The {@link IBeanInstanceFactory} implementation with the highest {@link Priority} is used inside {@link IBeanContext}.
 */
@Bean
public interface IBeanInstanceFactory {
  /**
   * @return an object instance for the bean or null if none was created
   */
  <T> T select(Class<T> queryClass, List<IBean<T>> regs);

  /**
   * @return all object instances for the beans, never null
   */
  <T> List<T> selectAll(Class<T> queryClass, List<IBean<T>> regs);
}
