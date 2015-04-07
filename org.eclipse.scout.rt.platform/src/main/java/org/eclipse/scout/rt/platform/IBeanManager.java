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

import org.eclipse.scout.commons.annotations.Replace;

/**
 * @since 5.2
 */
public interface IBeanManager {

  ReentrantReadWriteLock getReadWriteLock();

  /**
   * @param beanClazz
   * @return all registered beans regardless if they have a {@link Replace} annotation
   */
  <T> List<IBean<T>> getRegisteredBeans(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return the single bean that is of exact type beanClazz or subclasses of beanClazz with a {@link Replace}
   *         <p>
   *         When B extends A and B has a {@link Replace} then A is removed from the result
   *         <p>
   *         this is the bean used in {@link IBean#getInstance(Class)} and {@link BEANS#get(Class)}
   * @throws PlatformException
   *           when multiple beans exist or no bean exists.
   */
  <T> IBean<T> getBean(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return the single bean (or null) that is of exact type beanClazz or subclasses of beanClazz with a {@link Replace}
   *         <p>
   *         When B extends A and B has a {@link Replace} then A is removed from the result
   *         <p>
   *         this is the bean used in {@link IBean#getInstance(Class)} and {@link BEANS#opt(Class)}
   * @throws PlatformException
   *           when multiple beans exist
   */
  <T> IBean<T> optBean(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return beans that are of type beanClazz or subclasses of it
   *         <p>
   *         When B extends A and B has a {@link Replace} then A is removed from the result
   *         <p>
   *         these are the candidate beans used in {@link #getInstances(Class)}
   */
  <T> List<IBean<T>> getBeans(Class<T> beanClazz);

  /**
   * This is a convenience for {@link #registerBean(BeanMetaData)} and calls {@link #registerBean(BeanMetaData)} with a new
   * {@link BeanMetaData#BeanData(Class)}
   *
   * @param beanClazz
   * @return the registered {@link IBean}
   */
  <T> IBean<T> registerClass(Class<T> clazz);

  /**
   * This is a convenience for {@link #unregisterBean(IBean)} and unregisters all {@link IBean} with
   * {@link IBean#getBeanClazz()} == clazz
   *
   * @param beanClazz
   */
  <T> void unregisterClass(Class<T> clazz);

  /**
   * @param beanData
   * @return the registered {@link IBean}
   */
  <T> IBean<T> registerBean(BeanMetaData beanData);

  /**
   * @param bean
   */
  void unregisterBean(IBean<?> bean);
}
