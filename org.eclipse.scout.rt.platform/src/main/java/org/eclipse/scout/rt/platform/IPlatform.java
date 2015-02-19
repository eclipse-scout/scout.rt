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
 *
 */
public interface IPlatform {

  static enum State {
    /**
     * The state is active during initializing the system. Initializing is used to read system configurations and
     * register
     * static dependencies like services. During initialization registries are not available for use.
     */
    Initializing,
    /**
     * This state is active
     */
    Starting,
    Running,
    Stopping,
    Stopped
  }

  /**
   *
   */
  void start();

  /**
   *
   */
  void stop();

  /**
   * @return
   */
  State getState();

  /**
   * @param listener
   */
  void addPlatformListener(IPlatformListener listener);

  /**
   * @param listener
   */
  void removePlatformListener(IPlatformListener listener);

}
