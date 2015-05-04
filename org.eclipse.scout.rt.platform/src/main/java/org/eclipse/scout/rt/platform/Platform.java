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

import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is the main scout platform, automatically started
 * on first access to this class.
 * <p>
 * Tests use a PlatformTestRunner
 * <p>
 * When running in workspace, the jandex class scanner used in scout is automatically creating and caching the
 * target/classes/META-INF/jandex.idx files.
 * <p>
 * Use the system property <code>jandex.rebuild=true</code> in order to force a rebuild in case some beans were changed,
 * added or removed from the source code.
 */
public final class Platform {

  private static final ReentrantReadWriteLock INIT_LOCK = new ReentrantReadWriteLock(true);
  private static IPlatform platform;

  private Platform() {
  }

  /**
   * @return active platform
   *         <p>
   *         If there exists a java service config at META-INF/services/org.eclipse.scout.rt.platform.IPlatform then the
   *         platform is automatically started on the first hit of this {@link Platform} class by the class initializer.
   *         <p>
   *         Such a auto-start file is typically placed inside the webapp project at
   *         src/main/resources/META-INF/services/org.eclipse.scout.rt.platform.IPlatform
   *         <p>
   *         The default platform entry is <code>org.eclipse.scout.rt.platform.DefaultPlatform</code>
   *         <p>
   *         see also {@link ServiceLoader#load(Class)}
   */
  public static IPlatform get() {
    INIT_LOCK.readLock().lock();
    try {
      return platform;
    }
    finally {
      INIT_LOCK.readLock().unlock();
    }
  }

  /**
   * Set the active platform using a custom implementor (not recommended).
   * <p>
   * Be careful when using this method. It should only be called by the one and only initializer.
   * <p>
   * Typically the servlet context creator.
   */
  public static void set(IPlatform p) {
    INIT_LOCK.writeLock().lock();
    try {
      platform = p;
    }
    finally {
      INIT_LOCK.writeLock().unlock();
    }
  }

  /*
   * static initializer used for optional autostart, see {@link #get()}
   */
  static {
    INIT_LOCK.writeLock().lock();
    try {
      ServiceLoader<IPlatform> loader = ServiceLoader.load(IPlatform.class);
      for (IPlatform p : loader) {
        platform = p;
        break;
      }
      if (platform == null) {
        platform = new DefaultPlatform();
      }
      platform.start();
    }
    finally {
      INIT_LOCK.writeLock().unlock();
    }
  }
}
