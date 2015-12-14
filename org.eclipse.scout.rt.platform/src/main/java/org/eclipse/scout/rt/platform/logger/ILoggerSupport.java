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
package org.eclipse.scout.rt.platform.logger;

import org.slf4j.Logger;

/**
 * A logger support allows to interact with the particular slf4j logger implementations in use.
 * <p>
 * <b>Note:</b> This class is not a bean by intention. The appropriate logger support is determined and installed by
 * {@link LoggerPlatformListener} or manually by the scout project.
 *
 * @since 5.2
 */
public interface ILoggerSupport {

  /**
   * Log levels supported by scout. The levels of concrete loggers are mapped to and limited by these values.
   */
  public static enum LogLevel {
    ALL,
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF;
  }

  /**
   * Returns the scout log level of the logger with given name. Can be <code>null</code>.
   */
  LogLevel getLogLevel(String name);

  /**
   * Returns the scout log level of the given class' logger. Can be <code>null</code>.
   */
  LogLevel getLogLevel(Class<?> clazz);

  /**
   * Returns the scout log level of the given slf4j logger. Can be <code>null</code>.
   */
  LogLevel getLogLevel(Logger logger);

  /**
   * Sets the scout log level of the logger with given name.
   *
   * @param level
   *          a level or <code>null</code>.
   */
  void setLogLevel(String name, LogLevel level);

  /**
   * Sets the scout log level of the given class' logger.
   *
   * @param level
   *          a level or <code>null</code>.
   */
  void setLogLevel(Class<?> clazz, LogLevel level);

  /**
   * Sets the scout log level of the given slf4j logger
   *
   * @param level
   *          a level or <code>null</code>.
   */
  void setLogLevel(Logger logger, LogLevel level);
}
