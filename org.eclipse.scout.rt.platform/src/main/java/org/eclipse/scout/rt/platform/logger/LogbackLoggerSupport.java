/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ConfigurationEvent.EventType;

/**
 * <p>
 * Logger support for Logback.
 * </p>
 * <p>
 * With support for change tracking to automatically re-apply all changes after Logback reloads its logging
 * configuration (e.g. configured by periodic rescan)
 * </p>
 *
 * @since 5.2
 */
public class LogbackLoggerSupport extends AbstractLoggerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(LogbackLoggerSupport.class);

  /**
   * {@link Map} caching log levels for loggers whose level was changed dynamically after initialization.
   */
  private volatile Map<Logger, Optional<LogLevel>> m_changeMap = new ConcurrentHashMap<>();

  public LogbackLoggerSupport(ILoggerFactory factory) {
    LoggerContext loggerContext = Assertions.assertNotNull((LoggerContext) factory, "No {} provided", LoggerContext.class);

    loggerContext.addConfigurationEventListener(evt -> {
      if (evt.getEventType() == EventType.CONFIGURATION_ENDED_SUCCESSFULLY) {
        clearInitialStates(); // initial states will be set again with the following setLogLevel calls (however they might have changed after reload)

        // re-apply all dynamically set custom log-levels
        Map<Logger, Optional<LogLevel>> changeMap = m_changeMap;
        if (changeMap != null) {
          changeMap.forEach((k, v) -> setLogLevel(k, v.orElse(null)));
        }
      }
    });
  }

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
    trackInitialState(logger.getName());
    trackChange(logger, level);
    Level logbackLevel = scoutToLogbackLevel(level);
    ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
    logbackLogger.setLevel(logbackLevel);
  }

  @Override
  public synchronized void resetToInitialStates() {
    m_changeMap = null; // disable change tracking
    super.resetToInitialStates();
    m_changeMap = new ConcurrentHashMap<>(); // track changes again (empty may, levels are set to initial states again)
  }

  protected void trackChange(Logger logger, LogLevel level) {
    Map<Logger, Optional<LogLevel>> changeMap = m_changeMap;
    if (changeMap != null) {
      changeMap.put(logger, Optional.ofNullable(level));
    }
  }

  protected LogLevel logbackToScoutLevel(Level level) {
    if (level == null) {
      return null;
    }
    return switch (level.toInt()) {
      case Level.ALL_INT -> LogLevel.ALL;
      case Level.TRACE_INT -> LogLevel.TRACE;
      case Level.DEBUG_INT -> LogLevel.DEBUG;
      case Level.INFO_INT -> LogLevel.INFO;
      case Level.WARN_INT -> LogLevel.WARN;
      case Level.ERROR_INT -> LogLevel.ERROR;
      case Level.OFF_INT -> LogLevel.OFF;
      default -> {
        LOG.info("unknown logback level '{}'. Falling back to scout log level '{}'", level, LogLevel.WARN);
        yield LogLevel.WARN;
      }
    };
  }

  protected Level scoutToLogbackLevel(LogLevel level) {
    if (level == null) {
      return null;
    }
    return switch (level) {
      case ALL, TRACE -> Level.TRACE;
      case DEBUG -> Level.DEBUG;
      case INFO -> Level.INFO;
      case WARN -> Level.WARN;
      case ERROR -> Level.ERROR;
      case OFF -> Level.OFF;
      default -> {
        LOG.info("unknown scout log level '{}'. Falling back to logback level '{}'", level, Level.WARN);
        yield Level.WARN;
      }
    };
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
