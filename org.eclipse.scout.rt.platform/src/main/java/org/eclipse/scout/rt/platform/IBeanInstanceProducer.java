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

/**
 * Used in {@link IBean#getInstance(Class)}
 * <p>
 * Knows how to create an object instance of a {@link IBean}
 * <p>
 * see also {@link IBeanDecorationFactory}
 * <p>
 * Implementations of this interface must use an explicit generics parameter, no '? extend T' or '? super T' signtures.
 */
@Bean
public interface IBeanInstanceProducer<T> {
  /**
   * @return the new valid instance based on the bean and the current instance
   */
  <SUB extends T> SUB produceInstance(IBean<SUB> bean);
}
