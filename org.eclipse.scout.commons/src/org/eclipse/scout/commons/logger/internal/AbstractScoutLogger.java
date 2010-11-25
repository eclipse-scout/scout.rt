/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.logger.internal;

import java.util.HashSet;
import java.util.logging.LogRecord;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.JavaLogUtility;

public abstract class AbstractScoutLogger implements IScoutLogger {

  public AbstractScoutLogger() {
  }

  protected void log(int level, String format, Object[] args, Throwable t) {
    if (!isLoggable(level)) return;
    LogRecord record = JavaLogUtility.buildLogRecord(AbstractScoutLogger.class, JavaLogUtility.scoutToJavaLevel(level), format, args);
    record.setLoggerName(getName());
    record.setThrown(t);
    logImpl(record);
  }

  protected abstract void logImpl(LogRecord record);

  protected StackTraceElement getCallerLine(Class wrapperClass) {
    try {
      StackTraceElement[] trace = new Exception().getStackTrace();
      int traceIndex = 0;
      HashSet<String> ignoredPackagePrefixes = new HashSet<String>();
      ignoredPackagePrefixes.add(IScoutLogger.class.getPackage().getName());
      if (wrapperClass != null) {
        ignoredPackagePrefixes.add(wrapperClass.getPackage().getName());
      }
      while (traceIndex < trace.length) {
        boolean found = true;
        for (String prefix : ignoredPackagePrefixes) {
          if (trace[traceIndex].getClassName().startsWith(prefix)) {
            found = false;
            break;
          }
        }
        if (found) {
          break;
        }
        traceIndex++;
      }
      if (traceIndex >= trace.length) {
        traceIndex = trace.length - 1;
      }
      return trace[traceIndex];
    }
    catch (Throwable t) {
      return null;
    }
  }

  public void debug(String msg) {
    log(LEVEL_DEBUG, msg, null, null);
  }

  public void debug(String format, Object arg) {
    log(LEVEL_DEBUG, format, new Object[]{arg}, null);
  }

  public void debug(String format, Object arg1, Object arg2) {
    log(LEVEL_DEBUG, format, new Object[]{arg1, arg2}, null);
  }

  public void debug(String format, Object[] argArray) {
    log(LEVEL_DEBUG, format, argArray, null);
  }

  public void debug(String msg, Throwable t) {
    log(LEVEL_DEBUG, msg, null, t);
  }

  public void error(String msg) {
    log(LEVEL_ERROR, msg, null, null);
  }

  public void error(String format, Object arg) {
    log(LEVEL_ERROR, format, new Object[]{arg}, null);
  }

  public void error(String format, Object arg1, Object arg2) {
    log(LEVEL_ERROR, format, new Object[]{arg1, arg2}, null);
  }

  public void error(String format, Object[] argArray) {
    log(LEVEL_ERROR, format, argArray, null);
  }

  public void error(String msg, Throwable t) {
    log(LEVEL_ERROR, msg, null, t);
  }

  public void info(String msg) {
    log(LEVEL_INFO, msg, null, null);
  }

  public void info(String format, Object arg) {
    log(LEVEL_INFO, format, new Object[]{arg}, null);
  }

  public void info(String format, Object arg1, Object arg2) {
    log(LEVEL_INFO, format, new Object[]{arg1, arg2}, null);
  }

  public void info(String format, Object[] argArray) {
    log(LEVEL_INFO, format, argArray, null);
  }

  public void info(String msg, Throwable t) {
    log(LEVEL_INFO, msg, null, t);
  }

  public void trace(String msg) {
    log(LEVEL_DEBUG, msg, null, null);
  }

  public void trace(String format, Object arg) {
    log(LEVEL_DEBUG, format, new Object[]{arg}, null);
  }

  public void trace(String format, Object arg1, Object arg2) {
    log(LEVEL_DEBUG, format, new Object[]{arg1, arg2}, null);
  }

  public void trace(String format, Object[] argArray) {
    log(LEVEL_DEBUG, format, argArray, null);
  }

  public void trace(String msg, Throwable t) {
    log(LEVEL_DEBUG, msg, null, t);
  }

  public void warn(String msg) {
    log(LEVEL_WARN, msg, null, null);
  }

  public void warn(String format, Object arg) {
    log(LEVEL_WARN, format, new Object[]{arg}, null);
  }

  public void warn(String format, Object[] argArray) {
    log(LEVEL_WARN, format, argArray, null);
  }

  public void warn(String format, Object arg1, Object arg2) {
    log(LEVEL_WARN, format, new Object[]{arg1, arg2}, null);
  }

  public void warn(String msg, Throwable t) {
    log(LEVEL_WARN, msg, null, t);
  }

  public boolean isTraceEnabled() {
    return isLoggable(LEVEL_TRACE);
  }

  public boolean isDebugEnabled() {
    return isLoggable(LEVEL_DEBUG);
  }

  public boolean isInfoEnabled() {
    return isLoggable(LEVEL_INFO);
  }

  public boolean isWarnEnabled() {
    return isLoggable(LEVEL_WARN);
  }

  public boolean isErrorEnabled() {
    return isLoggable(LEVEL_ERROR);
  }

  public boolean isLoggable(int level) {
    return getLevel() >= level;
  }

}
