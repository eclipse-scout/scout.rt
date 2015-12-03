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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * The static accessor to the {@link IBeanManager}
 *
 * @see IBeanManager
 */
public final class BEANS {

  private BEANS() {
  }

  /**
   * Gets the single instance of the given beanClazz with respect to {@link Order} and {@link Replace}.<br>
   * See {@link IBeanManager#getBean(Class)} for details.
   *
   * @param beanClazz
   *          The query {@link Class}.
   * @return The bean instance. Never returns <code>null</code>.
   * @throws AssertionException
   *           When no instance is available or when multiple instances are registered.
   */
  public static <T> T get(Class<T> beanClazz) {
    return Assertions.assertNotNull(opt(beanClazz), "no instance found for query: %s", beanClazz);
  }

  /**
   * Gets the single instance of the given beanClazz with respect to {@link Order} and {@link Replace}.<br>
   * See {@link IBeanManager#getBean(Class)} for details.
   *
   * @param beanClazz
   *          The query {@link Class}.
   * @return The bean instance or <code>null</code> if no {@link IBean} could be found.
   * @throws AssertionException
   *           When multiple instances are registered
   */
  public static <T> T opt(Class<T> beanClazz) {
    IBean<T> bean = Platform.get().getBeanManager().optBean(beanClazz);
    if (bean != null) {
      return bean.getInstance();
    }
    return null;
  }

  /**
   * Gets all not replaced beans of the given beanClazz.<br>
   * See {@link IBeanManager#getBeans(Class)} for more details.
   *
   * @return All instances of this type ordered by {@link Order} annotation value. Never returns <code>null</code>.
   */
  public static <T> List<T> all(Class<T> beanClazz) {
    return BEANS.all(beanClazz, null);
  }

  /**
   * Gets all not replaced beans of the given beanClazz that are accepted by the given {@link IFilter}.<br>
   * See {@link IBeanManager#getBeans(Class)} for more details.
   *
   * @return All instances of this type ordered by {@link Order} annotation value. Never returns <code>null</code>.
   */
  public static <T> List<T> all(Class<T> beanClazz, IFilter<T> filter) {
    List<IBean<T>> beans = Platform.get().getBeanManager().getBeans(beanClazz);
    List<T> instances = new ArrayList<T>(beans.size());
    for (IBean<T> bean : beans) {
      T instance = bean.getInstance();
      if (instance != null && (filter == null || filter.accept(instance))) {
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
