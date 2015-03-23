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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.Assertions.AssertionException;

/**
 * @since 5.2
 */
public interface IBeanContext {

  ReentrantReadWriteLock getReadWriteLock();

  /**
   * @param beanClazz
   * @return the instance of the given bean, see {@link Bean}
   * @throws AssertionException
   *           when no bean is registered to the given beanClazz
   */
  <T> T getInstance(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return the instance of the given bean, see {@link Bean}
   * @throws AssertionException
   *           when no bean is registered to the given beanClazz
   */
  <T> T getInstanceOrNull(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return all instances, see {@link Bean}
   */
  <T> List<T> getInstances(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return
   */
  <T> List<IBean<T>> getBeans(Class<T> beanClazz);

  /**
   * This is a convenience for {@link #registerBean(BeanData)}
   *
   * @param beanClazz
   * @return the registered {@link IBean}
   */
  <T> IBean<T> registerClass(Class<T> clazz);

  /**
   * @param beanData
   * @return the registered {@link IBean}
   */
  <T> IBean<T> registerBean(BeanData beanData);

  /**
   * @param bean
   */
  void unregisterBean(IBean<?> bean);
}
