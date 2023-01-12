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

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * Logger support for Logback.
 *
 * @since 5.2
 */
public class LogbackLoggerSupport extends AbstractLoggerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(LogbackLoggerSupport.class);

  @Override
  public LogLevel getLogLevel(String name) {
    return getLogLevel(LoggerFactory.getLogger(name));
  }

  @Override
  public LogLevel getLogLevel(Logger logger) {
    ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
    Level logbackLevel = logbackLogger.getLevel();
    return logbackToScoutLevel(logbackLevel);
  }

  @Override
  public void setLogLevel(String name, LogLevel level) {
    setLogLevel(LoggerFactory.getLogger(name), level);
  }

  @Override
  public void setLogLevel(Logger logger, LogLevel level) {
    Level logbackLevel = scoutToLogbackLevel(level);
    ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
    logbackLogger.setLevel(logbackLevel);
  }

  protected LogLevel logbackToScoutLevel(Level level) {
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
        return LogLevel.ERROR;
      case Level.OFF_INT:
        return LogLevel.OFF;
      default:
        LOG.info("unknown logback level '{}'. Falling back to scout log level '{}'", level, LogLevel.WARN);
        return LogLevel.WARN;
    }
  }

  protected Level scoutToLogbackLevel(LogLevel level) {
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
        LOG.info("unknown scout log level '{}'. Falling back to logback level '{}'", level, Level.WARN);
        return Level.WARN;
    }
  }

  @Override
  public void shutdown() {
    // similar to ch.qos.logback.classic.servlet.LogbackServletContextListener.contextDestroyed(ServletContextEvent)
    // however we explicitly want to decide in which order/when this code is run
    ILoggerFactory factory = LoggerFactory.getILoggerFactory();
    if (factory instanceof LoggerContext) {
      LoggerContext loggerContext = (LoggerContext) factory;
      LOG.info("About to stop {}", loggerContext);
      loggerContext.stop();
    }
  }
}
