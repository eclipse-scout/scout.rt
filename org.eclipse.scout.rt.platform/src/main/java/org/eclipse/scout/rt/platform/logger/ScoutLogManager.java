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
package org.eclipse.scout.rt.platform.logger;

import org.eclipse.scout.rt.platform.logger.internal.Slf4jLogWrapper;

/**
 * The default factory to create {@link IScoutLogger} objects.
 * <p>
 * This factory creates transparent wrappers of SLF4J logger {@link org.slf4j.Logger}.<br>
 * Custom log managers are not supported anymore.
 *
 * @see IScoutLogger
 */
public final class ScoutLogManager {

  private ScoutLogManager() {
  }

  /**
   * To get a new instance of the log wrapper
   *
   * @param clazz
   * @return
   */
  public static IScoutLogger getLogger(Class clazz) {
    return new Slf4jLogWrapper(clazz.getName());
  }

  /**
   * To get a new instance of the log wrapper
   *
   * @param name
   * @return
   */
  public static IScoutLogger getLogger(String name) {
    return new Slf4jLogWrapper(name);
  }
}
