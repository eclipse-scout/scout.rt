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

import org.slf4j.Logger;

/**
 * common implementation of logger support.
 *
 * @since 5.2
 */
public abstract class AbstractLoggerSupport implements ILoggerSupport {

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

  @Override
  public void shutdown() {
    // nop
  }
}
