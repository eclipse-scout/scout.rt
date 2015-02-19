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

import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;

/**
 * The static accessor to the {@link BeanContext}
 */
public final class OBJ {

  private OBJ() {
  }

  public static <T> T NEW(Class<T> beanClazz, T defaultBean) {
    return CDI.getBeanContext().getInstance(beanClazz, defaultBean);
  }

  public static <T> T NEW(Class<T> beanClazz) {
    return CDI.getBeanContext().getInstance(beanClazz);
  }

  public static <T> IBean<T> register(Class<T> clazz) {
    return CDI.getBeanContext().register(clazz);
  }

  public static <T> List<T> ALL(Class<T> beanClazz) {
    return CDI.getBeanContext().getInstances(beanClazz);
  }

  public static void register(IBean<?> bean) {
    CDI.getBeanContext().register(bean);
  }

  public static void unregisterBean(Class<?> clazz) {
    CDI.getBeanContext().unregisterBean(clazz);
  }

  public static void unregisterBean(IBean<?> bean) {
    CDI.getBeanContext().unregisterBean(bean);
  }
}
