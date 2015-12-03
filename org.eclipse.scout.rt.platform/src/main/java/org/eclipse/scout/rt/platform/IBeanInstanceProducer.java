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
public interface IBeanInstanceProducer<T> {
  /**
   * Creates an instance for the given {@link IBean}.
   * 
   * @param bean
   *          The {@link IBean} to create the instance for.
   * @return The created instance or <code>null</code>.
   */
  T produce(IBean<T> bean);
}
