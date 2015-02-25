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
 * To register a module add an entry of the {@link IModule} subclasses qualified class name to the text file
 * META-INF/services/org.eclipse.scout.rt.platform.IModule in your pom module.
 */
public interface IModule {

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
