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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.internal.PlatformImplementor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 *
 */
public final class Platform {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Platform.class);
  /**
   * Singleton instance.
   */
  private static IPlatform platform;

  private Platform() {
  }

  /**
   * @return active platform
   */
  public static IPlatform get() {
    return platform;
  }

  /**
   * Set the active platform using the default implementor (highly recommended).
   * <p>
   * Be careful when using this method. It should only be called by the one and only initializer.
   * <p>
   * Typically the servlet context creator.
   */
  public static void setDefault() {
    set(new PlatformImplementor());
  }

  /**
   * Set the active platform using a custom implementor (not recommended).
   * <p>
   * Be careful when using this method. It should only be called by the one and only initializer.
   * <p>
   * Typically the servlet context creator.
   */
  public static void set(IPlatform p) {
    platform = p;
  }

  public static boolean isOsgiRunning() {
//    return StringUtility.hasText(System.getProperty("org.osgi.framework.version"));
    Bundle bundle = FrameworkUtil.getBundle(Platform.class);
    return bundle != null;
  }
}
