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
package org.eclipse.scout.commons.logger.internal.slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.status.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogWrapper implements IScoutLogger {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([0-9]*)\\}", Pattern.DOTALL);
  private final Logger m_logger;

  public Slf4jLogWrapper(String name) {
    m_logger = LoggerFactory.getLogger(name);
  }

  @Override
  public String getName() {
    return m_logger.getName();
  }

  @Override
  public int getLevel() {
    if (isTraceEnabled()) {
      return IScoutLogger.LEVEL_TRACE;
    }
    if (isDebugEnabled()) {
      return IScoutLogger.LEVEL_DEBUG;
    }
    if (isInfoEnabled()) {
      return IScoutLogger.LEVEL_INFO;
    }
    if (isWarnEnabled()) {
      return IScoutLogger.LEVEL_WARN;
    }
    if (isErrorEnabled()) {
      return IScoutLogger.LEVEL_ERROR;
    }
    return IScoutLogger.LEVEL_OFF;
  }

  @Override
  public void setLevel(int level) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isTraceEnabled() {
    return m_logger.isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return m_logger.isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return m_logger.isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return m_logger.isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return m_logger.isErrorEnabled();
  }

  @Override
  public boolean isLoggable(int level) {
    switch (level) {
      case IScoutLogger.LEVEL_TRACE:
        return isTraceEnabled();
      case IScoutLogger.LEVEL_DEBUG:
        return isDebugEnabled();
      case IScoutLogger.LEVEL_INFO:
        return isInfoEnabled();
      case IScoutLogger.LEVEL_WARN:
        return isWarnEnabled();
      case IScoutLogger.LEVEL_ERROR:
        return isErrorEnabled();
      default:
        return false;
    }
  }

  // trace
  @Override
  public void trace(String msg) {
    m_logger.trace(msg);
  }

  @Override
  public void trace(String msg, Throwable t) {
    m_logger.trace(msg, t);
  }

  @Override
  public void trace(String format, Object arg) {
    trace(format, new Object[]{arg});
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    trace(format, new Object[]{arg1, arg2});
  }

  @Override
  public void trace(String format, Object[] argArray) {
    m_logger.trace(adaptFormat(format), argArray);
  }

  // debug
  @Override
  public void debug(String msg) {
    m_logger.debug(msg);
  }

  @Override
  public void debug(String msg, Throwable t) {
    m_logger.debug(msg, t);
  }

  @Override
  public void debug(String format, Object arg) {
    debug(format, new Object[]{arg});
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    debug(format, new Object[]{arg1, arg2});
  }

  @Override
  public void debug(String format, Object[] argArray) {
    m_logger.debug(adaptFormat(format), argArray);
  }

// info
  @Override
  public void info(String msg) {
    m_logger.info(msg);
  }

  @Override
  public void info(String msg, Throwable t) {
    m_logger.info(msg, t);
  }

  @Override
  public void info(String format, Object arg) {
    info(format, new Object[]{arg});
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    info(format, new Object[]{arg1, arg2});
  }

  @Override
  public void info(String format, Object[] argArray) {
    m_logger.info(adaptFormat(format), argArray);
  }

// warn
  @Override
  public void warn(String msg) {
    m_logger.warn(msg);
  }

  @Override
  public void warn(String msg, Throwable t) {
    m_logger.warn(msg, t);
  }

  @Override
  public void warn(String format, Object arg) {
    warn(format, new Object[]{arg});
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    warn(format, new Object[]{arg1, arg2});
  }

  @Override
  public void warn(String format, Object[] argArray) {
    m_logger.warn(adaptFormat(format), argArray);
  }

  // error
  @Override
  public void error(String msg) {
    m_logger.error(msg);
  }

  @Override
  public void error(String msg, Throwable t) {
    m_logger.error(msg, t);
  }

  @Override
  public void error(String format, Object arg) {
    error(format, new Object[]{arg});
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    error(format, new Object[]{arg1, arg2});
  }

  @Override
  public void error(String format, Object[] argArray) {
    m_logger.error(adaptFormat(format), argArray);
  }

  @Override
  public void log(IStatus status) {
  }

  @Override
  public void log(IProcessingStatus status) {
  }

  protected String adaptFormat(String s) {
    if (!StringUtility.hasText(s)) {
      return s;
    }
    Matcher m = VARIABLE_PATTERN.matcher(s);
    return m.replaceAll("{}");
  }
}
