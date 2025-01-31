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

/**
 * Used in {@link IBean#getInstance()}
 * <p>
 * Knows how to create an object instance for a {@link IBean}.
 * <p>
 * May be provided using {@link BeanMetaData#withProducer(IBeanInstanceProducer)} and
 * {@link IBeanManager#registerBean(BeanMetaData)}.
 *
 * @since 5.1
 */
@FunctionalInterface
public interface IBeanInstanceProducer<T> {
  /**
   * Creates an instance for the given {@link IBean}.
   *
   * @param bean
   *     The {@link IBean} to create the instance for.
   * @return The created instance or <code>null</code>.
   */
  T produce(IBean<T> bean);
}
