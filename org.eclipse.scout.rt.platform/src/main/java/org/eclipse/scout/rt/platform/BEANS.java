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
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;

/**
 * The static accessor to the {@link BeanManagerImplementor}
 */
public final class BEANS {

  private BEANS() {
  }

  /**
   * @return the single instance of this type with respect to {@link Order} and {@link Replace}. See also
   *         {@link IBeanManager#getBean(Class)}
   * @throws AssertionException
   *           when no instance is available or when multiple instances are registered
   */
  public static <T> T get(Class<T> beanClazz) {
    return Assertions.assertNotNull(opt(beanClazz), "no instance found for query: %s", beanClazz);
  }

  /**
   * @return the single instance of this type with respect to {@link Order} and {@link Replace}. See also {@link Bean}
   *         <p>
   *         returns <code>null</code> when no instance is available
   * @throws AssertionException
   *           when multiple instances are registered
   */
  public static <T> T opt(Class<T> beanClazz) {
    IBean<T> bean = Platform.get().getBeanManager().optBean(beanClazz);
    if (bean != null) {
      return bean.getInstance(beanClazz);
    }
    return null;
  }

  /**
   * @return all instances of this type ordered by {@link Order} (never <code>null</code>)
   */
  public static <T> List<T> all(Class<T> beanClazz) {
    List<IBean<T>> beans = Platform.get().getBeanManager().getBeans(beanClazz);
    List<T> instances = new ArrayList<T>(beans.size());
    for (IBean<T> bean : beans) {
      T instance = bean.getInstance(beanClazz);
      if (instance != null) {
        instances.add(instance);
      }
    }
    return instances;
  }

  /**
   * @return the {@link IBeanManager} of the current {@link Platform#get()}
   */
  public static IBeanManager getBeanManager() {
    return Platform.get().getBeanManager();
  }
}
