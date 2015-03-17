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

import org.eclipse.scout.rt.platform.inventory.IClassInventory;

/**
 * All instances of IPlatformListener receive event notifications from the platform
 */
public interface IPlatform {

  static enum State {
    /**
     * This event signals that {@link IPlatform#start()} was called.
     * <p>
     * No state is valid so far
     * <p>
     * Next phase is creating the class inventory
     */
    PlatformInit,
    /**
     * This event signals that {@link IPlatform#getClassInventory()} is now valid.
     * <p>
     * Next phase is building the bean-context
     */
    ClassInventoryValid,
    /**
     * This event signals that {@link IPlatform#getBeanContext()} was prepared with the beans found in the
     * {@link IPlatform#getClassInventory()} and may manipulated using
     * {@link IBeanContext#registerBean(org.eclipse.scout.rt.platform.cdi.IBean, Object)} etc.
     * <p>
     * However, {@link IBeanContext#getInstance(Class)} is not available yet
     * <p>
     * Next phase is bean context valid
     */
    BeanContextPrepared,
    /**
     * This event signals that {@link IPlatform#getBeanContext()} is now valid and should not be manipulated anymore
     * <p>
     * {@link IBeanContext#getInstance(Class)} is valid now.
     * <p>
     * Next phase is starting the application
     */
    BeanContextValid,
    /**
     * This event signals that the platform is about to start the application, special init code that requires the valid
     * platform may be run now (former Activator.start logic)
     * <p>
     * Next phase is platform started
     */
    ApplicationStarting,
    /**
     * This event signals that the platform has completed starting the application {@link IApplication#start()}
     */
    ApplicationStarted,

    /**
     * This event signals that {@link IPlatform#stop()} was called.
     * <p>
     * Special dispose code may be run now (former Activator.stop logic)
     * <p>
     * Next phase is application stopped
     */
    ApplicationStopping,
    /**
     * application was stopped using {@link IApplication#stop()}
     * <p>
     * Next phase is platform stopped
     */
    ApplicationStopped,
    /**
     * platform is now stopped and all resources and caches are released and disposed
     */
    PlatformStopped
  }

  /**
   * @return
   */
  State getState();

  IClassInventory getClassInventory();

  IBeanContext getBeanContext();

  void start() throws PlatformException;

  /**
   *
   */
  void stop() throws PlatformException;
}
