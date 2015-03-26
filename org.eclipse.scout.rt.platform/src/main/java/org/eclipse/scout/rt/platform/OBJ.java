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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;

/**
 * The static accessor to the {@link BeanManagerImplementor}
 */
//TODO imo rename to BEANS
public final class OBJ {

  private OBJ() {
  }

  /**
   * @return the single instance of this type with respect to {@link Priority} and {@link Replace}. See also
   *         {@link IBeanContext#getBean(Class)}
   * @throws AssertionException
   *           when no instance is available or when multiple instances are registered
   */
  public static <T> T get(Class<T> beanClazz) {
    T instance = getOptional(beanClazz);
    if (instance != null) {
      return instance;
    }
    throw new Assertions.AssertionException("no instance found for query: " + beanClazz);
  }

  /**
   * @return the single instance of this type with respect to {@link Priority} and {@link Replace}. See also
   *         {@link Bean}
   *         <p>
   *         returns null when no instance is available
   * @throws AssertionException
   *           when multiple instances are registered
   */
//TODO imo rename to opt
  public static <T> T getOptional(Class<T> beanClazz) {
    IBean<T> bean = Platform.get().getBeanContext().optBean(beanClazz);
    if (bean != null) {
      return bean.getInstance();
    }
    return null;
  }

  /**
   * @return all instances of this type ordered by {@link Order}
   */
  public static <T> List<T> all(Class<T> beanClazz) {
    List<IBean<T>> beans = Platform.get().getBeanContext().getBeans(beanClazz);
    ArrayList<T> instances = new ArrayList<T>(beans.size());
    for (IBean<T> bean : beans) {
      T instance = bean.getInstance();
      if (instance != null) {
        instances.add(instance);
      }
    }
    return instances;
  }
}
