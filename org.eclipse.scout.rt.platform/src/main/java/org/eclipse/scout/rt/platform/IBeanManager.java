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

import java.util.List;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Manages all {@link IBean}s in a Scout application.<br>
 * {@link IBean}s are Java classes with additional meta information describing the bean (annotations).<br>
 * <br>
 * The {@link IBeanManager} is a dynamic registry of {@link IBean}s which allows to register, unregister and retrieve
 * {@link IBean}s based on their Java {@link Class}.
 *
 * @since 5.1
 * @see IBean
 */
public interface IBeanManager {

  /**
   * @param beanClazz
   *          The {@link Class} defining the class hierarchy to return.
   * @return All {@link IBean}s below the given class hierarchy regardless if they have a {@link Replace} annotation or
   *         not.
   */
  <T> List<IBean<T>> getRegisteredBeans(Class<T> beanClazz);

  /**
   * Lookup the exact {@link IBean} for the specified {@code beanClazz}. The {@link IBean} is returned even if the
   * {@code beanClazz} was replaced by another bean implementation or is <b>not</b> the most specific bean for the
   * specified {@code beanClazz}.
   *
   * @param beanClazz
   *          The {@link Class} defining the bean to return.
   * @return {@link IBean} with exact matching {@code beanClazz} or null if no matching bean could be found
   */
  <T> IBean<T> getRegisteredBean(Class<?> beanClazz);

  /**
   * Gets the most specific {@link IBean} for the given {@link Class}.<br>
   * The most specific bean means: Of all {@link IBean}s that are part of the class hierarchy spanned by the given
   * beanClazz the one is returned that:
   * <ul>
   * <li>Has not been replaced by a child class (using the {@link Replace} annotation)</li>
   * <li>and is closest to the beanClazz (according to the class hierarchy of beanClazz)</li>
   * <li>and has the lowest {@link Order} annotation value</li>
   * </ul>
   * <b>Please note:</b>
   * <ul>
   * <li>This means if there is an {@link IBean} available which is not replaced and exactly matches the given
   * beanClass, this exact match is returned even if there are child-classes with lower order available!</li>
   * <li>This method throws an {@link AssertionException} if there is not a unique result available (e.g. if several
   * beans on the same inheritance level have the same order) or if no {@link IBean} could be found. Therefore this
   * method never returns <code>null</code>.
   * <li>This is the {@link IBean} used in {@link BEANS#get(Class)}</li>
   * </ul>
   *
   * @param beanClazz
   *          The {@link Class} defining the class hierarchy to search the {@link IBean} in.
   * @return The most specific {@link IBean} according to the description above.
   * @throws AssertionException
   *           When the result is not unique or no {@link IBean} could be found.
   */
  <T> IBean<T> getBean(Class<T> beanClazz);

  /**
   * Gets the most specific {@link IBean} for the given {@link Class} but returns <code>null</code> instead of throwing
   * an {@link AssertionException} if no {@link IBean} could be found. Therefore this method can be used if an
   * {@link IBean} may be present or not.<br>
   * For a definition of the most specific {@link IBean} see {@link #getBean(Class)}.
   *
   * @param beanClazz
   *          The {@link Class} defining the class hierarchy to search the {@link IBean} in.
   * @return The most specific {@link IBean} or <code>null</code> if no {@link IBean} could be found.
   * @throws AssertionException
   *           When the result is not unique.
   */
  <T> IBean<T> optBean(Class<T> beanClazz);

  /**
   * Gets the most specific {@link IBean} for the given {@link Class} but returns <code>null</code> instead of throwing
   * an {@link AssertionException} if no {@link IBean} could be found <b>or the found bean is not unique</b>. Therefore
   * this method can be used if an {@link IBean} may be present or the {@link IBean} may not be unique.<br>
   * For a definition of the most specific {@link IBean} see {@link #getBean(Class)}.
   *
   * @param beanClazz
   *          The {@link Class} defining the class hierarchy to search the {@link IBean} in.
   * @return The most specific {@link IBean} or <code>null</code> if no {@link IBean} or no unique {@link IBean} could
   *         be found.
   */
  <T> IBean<T> uniqueBean(Class<T> beanClazz);

  /**
   * Gets all not replaced {@link IBean}s that are part of the given beanClazz hierarchy sorted by {@link Order}
   * annotation value.
   *
   * @param beanClazz
   *          The {@link Class} defining the class hierarchy to return.
   * @return A {@link List} holding all {@link IBean}s that are part of the given class hierarchy and have not been
   *         replaced by a child class (using {@link Replace} annotation).
   */
  <T> List<IBean<T>> getBeans(Class<T> beanClazz);

  /**
   * Checks whether there is a registered bean for the specified {@code clazz}.
   *
   * @return {@code true} if {@code clazz} is a registered bean (even if the {@code clazz} is replaced by another bean),
   *         else {@code false}
   */
  <T> boolean isBean(Class<T> clazz);

  /**
   * This is a convenience for {@link IBeanManager#registerBean(BeanMetaData)} and calls
   * {@link IBeanManager#registerBean(BeanMetaData)} with a new {@link BeanMetaData#BeanMetaData(Class)}
   *
   * @param clazz
   *          The bean class to register.
   * @return the registered {@link IBean}.
   */
  <T> IBean<T> registerClass(Class<T> clazz);

  /**
   * This is a convenience for {@link IBeanManager#unregisterBean(IBean)} and unregisters all {@link IBean}s with
   * {@link IBean#getBeanClazz()} == clazz
   *
   * @param clazz
   *          The class describing which {@link IBean}s to unregister.
   */
  <T> void unregisterClass(Class<T> clazz);

  /**
   * Registers a new {@link IBean} with the given bean description.
   *
   * @param beanData
   *          The bean description used to register a new {@link IBean}.
   * @return The newly registered {@link IBean}.
   */
  <T> IBean<T> registerBean(BeanMetaData beanData);

  /**
   * Unregisters the given {@link IBean}.
   *
   * @param bean
   *          The {@link IBean} to unregister.
   */
  void unregisterBean(IBean<?> bean);
}
