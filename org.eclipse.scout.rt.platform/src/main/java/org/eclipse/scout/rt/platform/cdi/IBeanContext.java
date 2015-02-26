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

/**
 *
 */
public interface IBeanContext {

  /**
   * @param beanClazz
   * @return
   */
  <T> IBean<T> registerClass(Class<T> clazz);

  /**
   * @param bean
   */
  void registerBean(IBean<?> bean);

  /**
   * @param bean
   */
  void unregisterBean(IBean<?> bean);

  /**
   * @param beanClazz
   * @return
   */
  <T> List<T> getInstances(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return
   */
  <T> T getInstance(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @param defaultBeanClazz
   * @return
   *         <p>
   *         TODO aho remove this method once cdi is in place
   */
  <T> T getInstance(Class<T> beanClazz, Class<? extends T> defaultBeanClazz);

  /**
   * @param beanClazz
   * @return
   */
  <T> List<IBean<T>> getBeans(Class<T> beanClazz);

  /**
   * @param beanClazz
   * @return
   */
  <T> IBean<T> getBean(Class<T> beanClazz);

  /**
   * @return
   */
  List<IBean<?>> getAllRegisteredBeans();

}
