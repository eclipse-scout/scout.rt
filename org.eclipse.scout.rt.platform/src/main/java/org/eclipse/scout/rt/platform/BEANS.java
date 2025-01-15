/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
   *     The query {@link Class}.
   * @return The bean instance. Never returns <code>null</code>.
   * @throws AssertionException
   *     When no instance is available or when multiple instances are registered.
   */
  public static <T> T get(Class<T> beanClazz) {
    return Assertions.assertNotNull(opt(beanClazz), "no instance found for query: {}", beanClazz);
  }

  /**
   * Gets the single instance of the given beanClazz with respect to {@link Order} and {@link Replace}.<br>
   * See {@link IBeanManager#getBean(Class)} for details.
   *
   * @param beanClazz
   *     The query {@link Class}.
   * @return The bean instance or <code>null</code> if no {@link IBean} could be found.
   * @throws AssertionException
   *     When multiple instances are registered
   */
  public static <T> T opt(Class<T> beanClazz) {
    IBean<T> bean = Platform.get().getBeanManager().optBean(beanClazz);
    if (bean != null) {
      return bean.getInstance();
    }
    return null;
  }

  /**
   * Gets the single instance of the given bean class with respect to {@link Order} and {@link Replace}.<br>
   * See {@link IBeanManager#getBean(Class)} for details.
   *
   * @param beanClass
   *     The query {@link Class}.
   * @return An {@link Optional} holding the bean instance or an empty {@link Optional} if no instance could be found.
   * @throws AssertionException
   *     if multiple instances are registered
   */
  public static <T> Optional<T> optional(Class<T> beanClass) {
    return Optional.ofNullable(opt(beanClass));
  }

  /**
   * Gets all not replaced beans of the given beanClazz.<br>
   * See {@link IBeanManager#getBeans(Class)} for more details.
   *
   * @return All instances of this type ordered by {@link Order} annotation value. Never returns <code>null</code>.
   */
  public static <T> List<T> all(Class<T> beanClazz) {
    return all(beanClazz, null);
  }

  /**
   * Gets all not replaced beans of the given beanClazz that are accepted by the given {@link Predicate}.<br>
   * See {@link IBeanManager#getBeans(Class)} for more details.
   *
   * @return All instances of this type ordered by {@link Order} annotation value. Never returns <code>null</code>.
   */
  public static <T> List<T> all(Class<T> beanClazz, Predicate<T> filter) {
    List<IBean<T>> beans = Platform.get().getBeanManager().getBeans(beanClazz);
    List<T> instances = new ArrayList<>(beans.size());
    for (IBean<T> bean : beans) {
      T instance = bean.getInstance();
      if (instance != null && (filter == null || filter.test(instance))) {
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
