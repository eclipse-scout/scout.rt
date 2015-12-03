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

import java.util.ServiceLoader;

import org.eclipse.scout.rt.platform.internal.PlatformStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main scout platform, automatically started on first access to this class.
 * <p>
 * The default platform implementor is the {@link DefaultPlatform}. A different platform instance can be set by
 * registering the fully qualified class name in a java service config at
 * <i>META-INF/services/org.eclipse.scout.rt.platform.IPlatform</i> (see {@link ServiceLoader#load(Class)}). Such a file
 * is typically placed inside the webapp project at <i>src/main/resources/META-INF/...</i>. <b>Warning:</b> if multiple
 * config files are present on the classpath, only one of them is used to automatically initialize the platform.
 * <p>
 * Tests use a PlatformTestRunner.
 */
public final class Platform {
  private static final Logger LOG = LoggerFactory.getLogger(Platform.class);

  private static IPlatform platform;

  private Platform() {
  }

  /**
   * @return the active platform. It is automatically started when accessing this class (static initializer).
   */
  public static IPlatform get() {
    return platform;
  }

  /**
   * Set the active platform using a custom implementor (not recommended).
   * <p>
   * Be careful when using this method. It should only be called by the one and only initializer. In most cases,
   * replacing the platform should not be necessary. If needed, the use of a java service config file is recommended
   * (see class documentation for details).
   *
   * @see Platform
   */
  public static void set(IPlatform p) {
    platform = p;
  }

  // static initializer used for platform auto-start
  static {
    ServiceLoader<IPlatform> loader = ServiceLoader.load(IPlatform.class);
    for (IPlatform p : loader) {
      platform = p;
      break;
    }
    if (platform == null) {
      platform = new DefaultPlatform();
    }
    PlatformStateLatch platformStateLatch = new PlatformStateLatch();
    // Start platform initialization in separate thread to let class initialization complete
    new PlatformStarter(platform, platformStateLatch).start();
    try {
      platformStateLatch.await();
    }
    catch (InterruptedException e) {
      LOG.error("Interrupted while waiting for platform state latch", e);
    }
  }
}
