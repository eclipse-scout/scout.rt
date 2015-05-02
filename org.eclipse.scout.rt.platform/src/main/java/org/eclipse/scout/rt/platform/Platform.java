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

/**
 * This is the main scout platform, typically installed {@link Platform#setDefault()} and started
 * {@link IPlatform#start(Class)} from within a servlet listener.
 * <p>
 * If there exists a java service config at META-INF/services/org.eclipse.scout.rt.platform.IPlatform then the platform
 * is automatically started on the first hit of this {@link Platform} class by the class initializer. See also
 * {@link #get()}
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

  /**
   * Singleton instance.
   */
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
    set(new DefaultPlatform());
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

  /*
   * static initializer used for optional autostart, see {@link #get()}
   */
  static {
    ServiceLoader<IPlatform> loader = ServiceLoader.load(IPlatform.class);
    for (IPlatform autostartPlatform : loader) {
      set(autostartPlatform);
      autostartPlatform.start();
      break;
    }
  }
}
