/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import org.slf4j.Logger;

/**
 * A logger support allows to interact with the particular slf4j logger implementations in use.
 * <p>
 * <b>Note:</b> This class is not a bean by intention. The appropriate logger support is determined and installed by
 * {@link LoggerInstallPlatformListener} or manually by the scout project.
 *
 * @since 5.2
 */
public interface ILoggerSupport {

  /**
   * Log levels supported by scout. The levels of concrete loggers are mapped to and limited by these values.
   */
  enum LogLevel {
    ALL,
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF
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
   *     a level or <code>null</code>.
   */
  void setLogLevel(String name, LogLevel level);

  /**
   * Sets the scout log level of the given class' logger.
   *
   * @param level
   *     a level or <code>null</code>.
   */
  void setLogLevel(Class<?> clazz, LogLevel level);

  /**
   * Sets the scout log level of the given slf4j logger
   *
   * @param level
   *     a level or <code>null</code>.
   */
  void setLogLevel(Logger logger, LogLevel level);

  /**
   * Thread-safe. Enable initial state tracking (to enable restoring initial states after log levels have been changed).
   *
   * @see #resetToInitialStates()
   */
  void trackInitialStates();

  /**
   * Thread-safe. Resets all changed log-levels to their previous (initial) state (before any changes). Also disables
   * initial state-tracking ({@link #trackInitialStates()} must be called to re-enable it).
   *
   * @see #trackInitialStates()
   */
  void resetToInitialStates();

  /**
   * Shutdown the logger, called during shutdown to flush and/or free resources. Method should not throw if logger is
   * not even started (actually all known loggers do not have a state or are started implicitly by using them), however
   * some loggers should be explicitly shutdown. This method is called by a platform listener as late as possible during
   * shutdown as there is no guarantee that any more logging is possible after this call.
   */
  void shutdown();
}
