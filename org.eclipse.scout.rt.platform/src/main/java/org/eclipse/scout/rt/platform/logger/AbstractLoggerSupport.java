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
}
