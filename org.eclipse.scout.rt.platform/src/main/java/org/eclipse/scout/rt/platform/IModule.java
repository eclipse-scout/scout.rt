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
 * A {@link IModule} can be used to take part at the scout platform's lifecycle.
 * To register a module use the <b><code>scout.module</code></b> entry in the maifest file.
 */
public interface IModule {
  /**
   * The Manifest.MF entry to provide a module.</br>
   *
   * <pre>
   * MANIFEST.MF
   * Scout-Module: fullyQuallifiedModuleName
   * </pre>
   */
  static String MANIFEST_MODULE_ENTRY = "Scout-Module";

  /**
   * will be called of the platform launcher {@link Launcher} during startup. This method is used to set up the system,
   * configuration and dependencies.
   * Do not use any registries e.g. service registry during module start up.
   */
  void start();

  /**
   * Is called before the {@link Platform} shut down. This method can be used to deregister dependencies and system
   * configurations.
   */
  void stop();
}
