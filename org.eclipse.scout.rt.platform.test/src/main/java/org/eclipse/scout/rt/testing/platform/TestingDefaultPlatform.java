/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform;

import org.eclipse.scout.rt.platform.DefaultPlatform;
import org.eclipse.scout.rt.platform.internal.BeanFilter;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.logger.LoggerShutdownPlatformListener;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;

/**
 * Replacement for {@link DefaultPlatform} which is used for {@link RunWithNewPlatform} if no other platform is
 * specified.
 * <p>
 * This platform offers an {@link #acceptBean(Class)} method to exclude specific beans from being added to the bean
 * manager (e.g. some or all platform listeners).
 * </p>
 */
public class TestingDefaultPlatform extends DefaultPlatform {

  @Override
  protected BeanManagerImplementor createBeanManager() {
    BeanManagerImplementor context = newBeanManagerImplementor();
    new BeanFilter().collect(ClassInventory.get())
        .stream()
        .filter(this::acceptBean)
        .forEach(context::registerClass);
    return context;
  }

  protected boolean acceptBean(Class<?> bean) {
    if (LoggerShutdownPlatformListener.class.isAssignableFrom(bean)) {
      // do not register this platform listener if multiple platforms may be started and stopped again
      // as logger shutdown may also trigger logger shutdown outside platform (e.g. if one platform is
      // used while another has been already started
      return false;
    }
    return true;
  }
}
