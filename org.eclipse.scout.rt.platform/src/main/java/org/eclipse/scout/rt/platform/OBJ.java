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

import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.internal.BeanContextImplementor;

/**
 * The static accessor to the {@link BeanContextImplementor}
 */
public final class OBJ {

  private OBJ() {
  }

  /**
   * @return the first instance of this type with respect to {@link Priority} and {@link Replace}. See also {@link Bean}
   * @throws PlatformException
   *           when no instance is available
   */
  public static <T> T get(Class<T> beanClazz) {
    return Platform.get().getBeanContext().getInstance(beanClazz);
  }

  /**
   * @return the first instance of this type with respect to {@link Priority} and {@link Replace}. See also {@link Bean}
   *         <p>
   *         returns null when no instance is available
   */
  public static <T> T getOptional(Class<T> beanClazz) {
    return Platform.get().getBeanContext().getInstanceOrNull(beanClazz);
  }

  /**
   * @return all instances of this type ordered by {@link Priority}
   */
  public static <T> List<T> all(Class<T> beanClazz) {
    return Platform.get().getBeanContext().getInstances(beanClazz);
  }
}
