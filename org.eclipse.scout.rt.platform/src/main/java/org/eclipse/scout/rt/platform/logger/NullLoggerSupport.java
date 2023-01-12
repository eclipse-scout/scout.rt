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
  private final String m_slf4jLoggerFactoryClassName;

  public NullLoggerSupport(String slf4JLoggerFactoryClassName) {
    m_queriedLoggers = new ConcurrentHashMap<>();
    m_slf4jLoggerFactoryClassName = slf4JLoggerFactoryClassName;
  }

  protected ConcurrentMap<String, Object> getQueriedLoggers() {
    return m_queriedLoggers;
  }

  protected String getSlf4jLoggerFactoryClassName() {
    return m_slf4jLoggerFactoryClassName;
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
      LOG.info("getting or setting log level is not supported by current slf4j logging implementation [logger={}, slf4jLoggerFactoryClassName={}]", name, getSlf4jLoggerFactoryClassName());
    }
  }
}
