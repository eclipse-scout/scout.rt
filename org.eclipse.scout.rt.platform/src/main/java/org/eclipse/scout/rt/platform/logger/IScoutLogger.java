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

import java.util.logging.Logger;

import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * This interface can be used via {@link ScoutLogManager#getLogger(Class)}.
 * <p>
 * ScoutLogManager creates a transparent wrapper of an {@link org.slf4j.Logger}.
 * <p>
 * Therefore using this interface or {@link Logger#getLogger(String)} is the absolute same thing.
 * <p>
 */
public interface IScoutLogger {
  int LEVEL_OFF = 0;
  int LEVEL_ERROR = 1;
  int LEVEL_WARN = 2;
  int LEVEL_INFO = 3;
  int LEVEL_DEBUG = 4;
  int LEVEL_TRACE = 5;

  boolean isLoggable(int logLevel);

  /**
   * @return the name of this <code>IScoutLogger</code> instance.
   */
  String getName();

  int getLevel();

  void setLevel(int level);

  /**
   * Is the logger instance enabled for the TRACE level?
   *
   * @return True if this Logger is enabled for the TRACE level, false otherwise.
   * @since 1.4
   */
  boolean isTraceEnabled();

  /**
   * Log a message at the TRACE level.
   *
   * @param msg
   *          the message string to be logged
   * @since 1.4
   */
  void trace(String msg);

  /**
   * Log a message at the TRACE level according to the specified format and argument.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the TRACE level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg
   *          the argument
   * @since 1.4
   */
  void trace(String format, Object arg);

  /**
   * Log a message at the TRACE level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the TRACE level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   * @since 1.4
   */
  void trace(String format, Object arg1, Object arg2);

  /**
   * Log a message at the TRACE level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the TRACE level.
   * </p>
   *
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   * @since 1.4
   */
  void trace(String format, Object[] argArray);

  /**
   * Log an exception (throwable) at the TRACE level with an accompanying message.
   *
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   * @since 1.4
   */
  void trace(String msg, Throwable t);

  /**
   * Is the logger instance enabled for the DEBUG level?
   *
   * @return True if this Logger is enabled for the DEBUG level, false otherwise.
   */
  boolean isDebugEnabled();

  /**
   * Log a message at the DEBUG level.
   *
   * @param msg
   *          the message string to be logged
   */
  void debug(String msg);

  /**
   * Log a message at the DEBUG level according to the specified format and argument.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the DEBUG level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  void debug(String format, Object arg);

  /**
   * Log a message at the DEBUG level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the DEBUG level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  void debug(String format, Object arg1, Object arg2);

  /**
   * Log a message at the DEBUG level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the DEBUG level.
   * </p>
   *
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  void debug(String format, Object[] argArray);

  /**
   * Log an exception (throwable) at the DEBUG level with an accompanying message.
   *
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  void debug(String msg, Throwable t);

  /**
   * Is the logger instance enabled for the INFO level?
   *
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  boolean isInfoEnabled();

  /**
   * Log a message at the INFO level.
   *
   * @param msg
   *          the message string to be logged
   */
  void info(String msg);

  /**
   * Log a message at the INFO level according to the specified format and argument.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the INFO level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  void info(String format, Object arg);

  /**
   * Log a message at the INFO level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the INFO level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  void info(String format, Object arg1, Object arg2);

  /**
   * Log a message at the INFO level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the INFO level.
   * </p>
   *
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  void info(String format, Object[] argArray);

  /**
   * Log an exception (throwable) at the INFO level with an accompanying message.
   *
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  void info(String msg, Throwable t);

  /**
   * Is the logger instance enabled for the WARN level?
   *
   * @return True if this Logger is enabled for the WARN level, false otherwise.
   */
  boolean isWarnEnabled();

  /**
   * Log a message at the WARN level.
   *
   * @param msg
   *          the message string to be logged
   */
  void warn(String msg);

  /**
   * Log a message at the WARN level according to the specified format and argument.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the WARN level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  void warn(String format, Object arg);

  /**
   * Log a message at the WARN level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the WARN level.
   * </p>
   *
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  void warn(String format, Object[] argArray);

  /**
   * Log a message at the WARN level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the WARN level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  void warn(String format, Object arg1, Object arg2);

  /**
   * Log an exception (throwable) at the WARN level with an accompanying message.
   *
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  void warn(String msg, Throwable t);

  /**
   * Is the logger instance enabled for the ERROR level?
   *
   * @return True if this Logger is enabled for the ERROR level, false otherwise.
   */
  boolean isErrorEnabled();

  /**
   * Log a message at the ERROR level.
   *
   * @param msg
   *          the message string to be logged
   */
  void error(String msg);

  /**
   * Log a message at the ERROR level according to the specified format and argument.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the ERROR level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  void error(String format, Object arg);

  /**
   * Log a message at the ERROR level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the ERROR level.
   * </p>
   *
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  void error(String format, Object arg1, Object arg2);

  /**
   * Log a message at the ERROR level according to the specified format and arguments.
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for the ERROR level.
   * </p>
   *
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  void error(String format, Object[] argArray);

  /**
   * Log an exception (throwable) at the ERROR level with an accompanying message.
   *
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  void error(String msg, Throwable t);

  /**
   * Log the given {@link IStatus} in the level of the severity of the status ({@link IStatus#getSeverity()}).
   *
   * @param status
   *          The status to log. Must not be null.
   */
  void log(IStatus status);

  /**
   * Log the given {@link IProcessingStatus} in the level of the severity of the status (
   * {@link IProcessingStatus#getSeverity()}).
   *
   * @param status
   *          The {@link IProcessingStatus} to log. must not be null.
   */
  void log(IProcessingStatus status);
}
