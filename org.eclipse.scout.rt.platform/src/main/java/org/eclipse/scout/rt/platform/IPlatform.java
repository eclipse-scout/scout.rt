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

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;

/**
 * Scout platform life cycle manager.
 *
 * @since 5.2
 */
public interface IPlatform {

  /**
   * Enumeration describing the different platform states.
   */
  enum State {
    /**
     * This event signals that {@link IPlatform#getBeanManager()} was prepared with the beans found in the
     * {@link ClassInventory#get()} and may manipulated using {@link IBeanManager#registerBean(BeanMetaData)} etc.
     * <p>
     * However, {@link IBean#getInstance()} is not available yet
     */
    BeanManagerPrepared,

    /**
     * This event signals that {@link IPlatform#getBeanManager()} is now valid and should not be manipulated anymore
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
    PlatformStopped,

    /**
     * This event signals that the start of the platform failed and that the platform now is invalid.
     * <p>
     * If the platform is in this state, calls to {@link IPlatform#getBeanManager()} will result in an
     * {@link IllegalStateException}.
     */
    PlatformInvalid
  }

  /**
   * @return The current {@link State} of the platform.
   */
  State getState();

  /**
   * Returns the bean manager of the platform.
   *
   * @return The {@link IBeanManager} of the platform.
   * @throws PlatformException
   *           if the platform is in the {@link State#PlatformInvalid} state
   */
  IBeanManager getBeanManager();

  /**
   * Starts the platform.
   *
   * @throws PlatformException
   *           When the platform is already started or there is an error during startup.
   */
  void start();

  /**
   * Starts the platform.
   *
   * @param stateLatch
   *          an optional object that can be used to wait for the internal platform lock to be acquired. Consumers of
   *          this method may call {@link PlatformStateLatch#await()} to be blocked until the {@link IPlatform}
   *          implementor has acquired it's internal lock.
   *          <p>
   *          If no such synchronization is required, use {@link #start()}.
   * @throws PlatformException
   *           When the platform is already started or there is an error during startup.
   */
  void start(PlatformStateLatch stateLatch);

  /**
   * Stops the platform and releases all resources.
   *
   * @throws IllegalArgumentException
   *           if the platform cannot be stopped because it is not running yet.
   */
  void stop();

  /**
   * Gets if the platform is running in development mode.<br>
   *
   * @return <code>true</code> if the platform is running in development mode. <code>false</code> otherwise.
   */
  boolean inDevelopmentMode();
}
