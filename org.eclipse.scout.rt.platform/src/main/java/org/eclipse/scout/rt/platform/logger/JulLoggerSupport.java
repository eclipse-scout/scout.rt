/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger support for java.util.logging-based Loggers.
 *
 * @since 5.2
 */
public class JulLoggerSupport extends AbstractLoggerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(JulLoggerSupport.class);

  private final Map<Level, LogLevel> m_julToScoutLevelMap;

  public JulLoggerSupport() {
    m_julToScoutLevelMap = new HashMap<>();
    registerLevelMappings(m_julToScoutLevelMap);
  }

  protected void registerLevelMappings(Map<Level, LogLevel> julToScoutLevelMap) {
    julToScoutLevelMap.put(Level.ALL, LogLevel.ALL);
    julToScoutLevelMap.put(Level.FINEST, LogLevel.TRACE);
    julToScoutLevelMap.put(Level.FINER, LogLevel.DEBUG);
    julToScoutLevelMap.put(Level.FINE, LogLevel.DEBUG);
    julToScoutLevelMap.put(Level.CONFIG, LogLevel.INFO);
    julToScoutLevelMap.put(Level.INFO, LogLevel.INFO);
    julToScoutLevelMap.put(Level.WARNING, LogLevel.WARN);
    julToScoutLevelMap.put(Level.SEVERE, LogLevel.ERROR);
    julToScoutLevelMap.put(Level.OFF, LogLevel.OFF);
  }

  @Override
  public LogLevel getLogLevel(String name) {
    java.util.logging.Logger julLogger = getJulLogger(name);
    Level julLevel = julLogger.getLevel();
    return julToScoutLevel(julLevel);
  }

  @Override
  public void setLogLevel(String name, LogLevel level) {
    Level julLevel = scoutToJulLevel(level);
    java.util.logging.Logger julLogger = getJulLogger(name);
    julLogger.setLevel(julLevel);
  }

  protected java.util.logging.Logger getJulLogger(String name) {
    if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
      name = "";
    }
    return java.util.logging.Logger.getLogger(name);
  }

  protected LogLevel julToScoutLevel(Level level) {
    if (level == null) {
      return null;
    }
    LogLevel l = m_julToScoutLevelMap.get(level);
    if (l == null) {
      LOG.info("unknown jul level '{}'. Falling back to scout log level '{}'", level, LogLevel.WARN);
      l = LogLevel.WARN;
    }
    return l;
  }

  protected Level scoutToJulLevel(LogLevel level) {
    if (level == null) {
      return null;
    }
    switch (level) {
      case ALL:
        return Level.ALL;
      case TRACE:
        return Level.FINEST;
      case DEBUG:
        return Level.FINE;
      case INFO:
        return Level.INFO;
      case WARN:
        return Level.WARNING;
      case ERROR:
        return Level.SEVERE;
      case OFF:
        return Level.OFF;
      default:
        LOG.info("unknown scout log level '{}'. Falling back to jul level '{}'", level, Level.WARNING);
        return Level.WARNING;
    }
  }
}
