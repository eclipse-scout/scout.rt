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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;

/**
 * The static accessor to the {@link BeanContext}
 */
public final class OBJ {

  private OBJ() {
  }

  /**
   * @return the first instance of this type, the one with the highest {@link Priority}
   */
  public static <T> T one(Class<T> beanClazz) {
    return Platform.get().getBeanContext().getInstance(beanClazz);
  }

  public static <T> T oneOrNull(Class<T> beanClazz) {
    return Platform.get().getBeanContext().getInstanceOrNull(beanClazz);
  }

  /**
   * @return all instances of this type
   */
  public static <T> List<T> all(Class<T> beanClazz) {
    return Platform.get().getBeanContext().getInstances(beanClazz);
  }

  public static <T> IBean<T> registerClass(Class<T> clazz) {
    return Platform.get().getBeanContext().registerClass(clazz);
  }

  /**
   * @param bean
   * @param instance
   *          optional initial raw instance value of the bean, undecorated, not intercepted by
   *          {@link IBeanInstanceFactory}
   */
  public static void registerBean(IBean<?> bean, Object instance) {
    Platform.get().getBeanContext().registerBean(bean, instance);
  }

  public static void unregisterBean(IBean<?> bean) {
    Platform.get().getBeanContext().unregisterBean(bean);
  }
}
