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
  <T> IBean<T> register(Class<T> beanClazz);

  /**
   * @param bean
   */
  void register(IBean<?> bean);

  /**
   * @param clazz
   */
  void unregisterBean(Class<?> clazz);

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
   * @param defaultBean
   * @return
   */
  <T> T getInstance(Class<T> beanClazz, T defaultBean);

}
