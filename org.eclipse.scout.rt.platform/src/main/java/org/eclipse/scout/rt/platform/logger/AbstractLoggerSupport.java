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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

/**
 * common implementation of logger support.
 *
 * @since 5.2
 */
public abstract class AbstractLoggerSupport implements ILoggerSupport {

  private volatile Map<String, Optional<LogLevel>> m_initialStateMap;

  @Override
  public LogLevel getLogLevel(Class<?> clazz) {
    return getLogLevel(assertNotNull(clazz).getName());
  }

  @Override
  public LogLevel getLogLevel(Logger logger) {
    return getLogLevel(assertNotNull(logger).getName());
  }

  @Override
  public void setLogLevel(Class<?> clazz, LogLevel level) {
    setLogLevel(assertNotNull(clazz).getName(), level);
  }

  @Override
  public void setLogLevel(Logger logger, LogLevel level) {
    setLogLevel(assertNotNull(logger).getName(), level);
  }

  /**
   * Adds the initial log-level of logger with given name to the initial state map, should be called by
   * {@link #setLogLevel(String, LogLevel)} function which actually changes the level.
   */
  protected void trackInitialState(String name) {
    Map<String, Optional<LogLevel>> initialStateMap = m_initialStateMap;
    if (initialStateMap != null) {
      initialStateMap.putIfAbsent(name, Optional.ofNullable(getLogLevel(name)));
    }
  }

  @Override
  public synchronized void trackInitialStates() {
    m_initialStateMap = new ConcurrentHashMap<>();
  }

  @Override
  public synchronized void resetToInitialStates() {
    Map<String, Optional<LogLevel>> initialStateMap = m_initialStateMap;
    if (initialStateMap != null) {
      m_initialStateMap = null;
      initialStateMap.forEach((k, v) -> setLogLevel(k, v.orElse(null)));
    }
  }

  /**
   * Clear the initial state tracking (however does not enable/disable it), should be called after initial states have
   * been reloaded (e.g. logger itself reloads its initial states).
   */
  protected void clearInitialStates() {
    Map<String, Optional<LogLevel>> initialStateMap = m_initialStateMap;
    if (initialStateMap != null) {
      initialStateMap.clear();
    }
  }

  @Override
  public void shutdown() {
    // nop
  }
}
