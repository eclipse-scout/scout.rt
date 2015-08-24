/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Indicates whether the osgi is running in offline mode or online mode
 * <p>
 * see {@link IService} for description of this indicator class
 */
public final class OfflineState {
  public static final ThreadLocal<Boolean> CURRENT = new ThreadLocal<>();

  private static boolean defaultValue = false;

  private OfflineState() {
  }

  public static boolean isOfflineDefault() {
    return defaultValue;
  }

  public static boolean isOnlineDefault() {
    return !isOfflineDefault();
  }

  /**
   * Sets the default state. Thread-specific values {@link #CURRENT} are not changed by this method.
   */
  public static void setOfflineDefault(boolean b) {
    defaultValue = b;
  }

  public static boolean isOfflineInCurrentThread() {
    Boolean var = CURRENT.get();
    if (var != null) {
      return var.booleanValue();
    }
    else {
      return isOfflineDefault();
    }
  }

  public static boolean isOnlineInCurrentThread() {
    return !isOfflineInCurrentThread();
  }
}
