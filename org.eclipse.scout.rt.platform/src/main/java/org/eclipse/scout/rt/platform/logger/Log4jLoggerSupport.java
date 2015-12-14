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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger support for Log4j.
 *
 * @since 5.2
 */
public class Log4jLoggerSupport extends AbstractLoggerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(Log4jLoggerSupport.class);

  @Override
  public LogLevel getLogLevel(String name) {
    org.apache.log4j.Logger log4jLogger = getLog4jLogger(name);
    Level log4jLevel = log4jLogger.getLevel();
    return log4jToScoutLevel(log4jLevel);
  }

  @Override
  public void setLogLevel(String name, LogLevel level) {
    Level log4jLevel = scoutToLog4jLevel(level);
    org.apache.log4j.Logger log4jLogger = getLog4jLogger(name);
    log4jLogger.setLevel(log4jLevel);
  }

  protected org.apache.log4j.Logger getLog4jLogger(String name) {
    if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
      return LogManager.getRootLogger();
    }
    return LogManager.getLogger(name);
  }

  protected LogLevel log4jToScoutLevel(Level level) {
    if (level == null) {
      return null;
    }
    switch (level.toInt()) {
      case Level.ALL_INT:
        return LogLevel.ALL;
      case Level.TRACE_INT:
        return LogLevel.TRACE;
      case Level.DEBUG_INT:
        return LogLevel.DEBUG;
      case Level.INFO_INT:
        return LogLevel.INFO;
      case Level.WARN_INT:
        return LogLevel.WARN;
      case Level.ERROR_INT:
      case Level.FATAL_INT:
        return LogLevel.ERROR;
      case Level.OFF_INT:
        return LogLevel.OFF;
      default:
        LOG.info("unknown log4j level '{}'. Falling back to scout log level '{}'", level, LogLevel.WARN);
        return LogLevel.WARN;
    }
  }

  protected Level scoutToLog4jLevel(LogLevel level) {
    if (level == null) {
      return null;
    }
    switch (level) {
      case ALL:
        return Level.ALL;
      case TRACE:
        return Level.TRACE;
      case DEBUG:
        return Level.DEBUG;
      case INFO:
        return Level.INFO;
      case WARN:
        return Level.WARN;
      case ERROR:
        return Level.ERROR;
      case OFF:
        return Level.OFF;
      default:
        LOG.info("unknown scout log level '{}'. Falling back to log4j level '{}'", level, Level.WARN);
        return Level.WARN;
    }
  }
}
