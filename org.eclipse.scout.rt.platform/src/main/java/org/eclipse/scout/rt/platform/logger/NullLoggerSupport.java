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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.2
 */
public class NullLoggerSupport extends AbstractLoggerSupport {

  private static final Logger LOG = LoggerFactory.getLogger(NullLoggerSupport.class);
  private static final Object PRESENT = new Object();

  private final ConcurrentMap<String, Object> m_queriedLoggers;
  private final String m_slf4jloggerFactoryClassStr;

  public NullLoggerSupport(String slf4jloggerFactoryClassStr) {
    m_queriedLoggers = new ConcurrentHashMap<>();
    m_slf4jloggerFactoryClassStr = slf4jloggerFactoryClassStr;
  }

  protected ConcurrentMap<String, Object> getQueriedLoggers() {
    return m_queriedLoggers;
  }

  protected String getSlf4jloggerFactoryClassStr() {
    return m_slf4jloggerFactoryClassStr;
  }

  @Override
  public LogLevel getLogLevel(String name) {
    logLoggerSupportNotAvailable(name);
    return null;
  }

  @Override
  public void setLogLevel(String name, LogLevel level) {
    logLoggerSupportNotAvailable(name);
  }

  private void logLoggerSupportNotAvailable(String name) {
    if (LOG.isInfoEnabled() && getQueriedLoggers().putIfAbsent(name, PRESENT) == null) {
      LOG.info("getting or setting log level is not supported by current slf4j logging implementation [logger={}, slf4jLoggerFactoryClassStr={}]", name, getSlf4jloggerFactoryClassStr());
    }
  }
}
