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

import java.util.concurrent.CountDownLatch;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * All instances of IPlatformListener receive event notifications from the platform
 */
public interface IPlatform {

  enum State {
    /**
     * This event signals that {@link IPlatform#getBeanContext()} was prepared with the beans found in the
     * {@link IPlatform#getClassInventory()} and may manipulated using
     * {@link IBeanContext#registerBean(org.eclipse.scout.rt.platform.IBean, Object)} etc.
     * <p>
     * However, {@link IBean#getInstance()} is not available yet
     */
    BeanManagerPrepared,

    /**
     * This event signals that {@link IPlatform#getBeanContext()} is now valid and should not be manipulated anymore
     * <p>
     * {@link IBean#getInstance()} is valid now.
     */
    BeanManagerValid,

    /**
     * This event signals that the platform has completed starting and is ready.
     */
    PlatformStarted,

    /**
     * This event signals that {@link IPlatform#stop()} was called.
     * <p>
     * Special dispose code may be run now.
     */
    PlatformStopping,

    /**
     * platform is now stopped and all resources and caches are released and disposed
     */
    PlatformStopped
  }

  /**
   * @return
   */
  State getState();

  IBeanManager getBeanManager();

  void start(CountDownLatch waitForStartLockAcquire) throws PlatformException;

  void stop(CountDownLatch waitForStartLockAcquire) throws PlatformException;

  boolean inDevelopmentMode();
}
