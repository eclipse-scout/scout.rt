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
package org.eclipse.scout.rt.platform.cdi;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.Assertions.AssertionException;

/**
 *
 */
public interface IBeanContext {

  /**
   * @param beanClazz
   * @return the instance of the given bean.
   * @throws AssertionException
   *           when no bean is registered to the given beanClazz
   */
  <T> T getInstance(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return the instance of the given bean.
   * @throws AssertionException
   *           when no bean is registered to the given beanClazz
   */
  <T> T getInstanceOrNull(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return
   */
  <T> List<T> getInstances(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return
   */
  <T> List<IBean<T>> getBeans(Class<T> beanClazz);

  /**
   * @return
   */
  Set<IBean<?>> getAllRegisteredBeans();

  /**
   * @param beanClazz
   * @return
   */
  <T> IBean<T> registerClass(Class<T> clazz);

  /**
   * @param bean
   * @param instance
   *          optional initial raw instance value of the bean, undecorated, not intercepted by
   *          {@link IBeanInstanceFactory}
   */
  void registerBean(IBean bean, Object instance);

  /**
   * @param bean
   */
  void unregisterBean(IBean bean);
}
