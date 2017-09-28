/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * @since 5.2
 */
public class LogbackLoggerSupportTest {

  private static final Class<?> TEST_LOGGER_CLASS = LogbackLoggerSupportTest.class;
  private static final String TEST_LOGGER_NAME = TEST_LOGGER_CLASS.getName();
  private static final Logger TEST_LOGGER = LoggerFactory.getLogger(TEST_LOGGER_CLASS);

  private LogbackLoggerSupport m_loggerSupport;

  @Before
  public void before() {
    m_loggerSupport = new LogbackLoggerSupport();
  }

  @After
  public void after() {
    // reset log level
    setLogbackLevel(null);
  }

  @Test
  public void testScoutToLog4jLevel() {
    assertNull(m_loggerSupport.scoutToLogbackLevel(null));
    assertEquals(Level.ALL, m_loggerSupport.scoutToLogbackLevel(LogLevel.ALL));
    assertEquals(Level.TRACE, m_loggerSupport.scoutToLogbackLevel(LogLevel.TRACE));
    assertEquals(Level.DEBUG, m_loggerSupport.scoutToLogbackLevel(LogLevel.DEBUG));
    assertEquals(Level.INFO, m_loggerSupport.scoutToLogbackLevel(LogLevel.INFO));
    assertEquals(Level.WARN, m_loggerSupport.scoutToLogbackLevel(LogLevel.WARN));
    assertEquals(Level.ERROR, m_loggerSupport.scoutToLogbackLevel(LogLevel.ERROR));
    assertEquals(Level.OFF, m_loggerSupport.scoutToLogbackLevel(LogLevel.OFF));
  }

  @Test
  public void testLog4jToScoutLevel() {
    assertNull(m_loggerSupport.logbackToScoutLevel(null));
    assertEquals(LogLevel.ALL, m_loggerSupport.logbackToScoutLevel(Level.ALL));
    assertEquals(LogLevel.TRACE, m_loggerSupport.logbackToScoutLevel(Level.TRACE));
    assertEquals(LogLevel.DEBUG, m_loggerSupport.logbackToScoutLevel(Level.DEBUG));
    assertEquals(LogLevel.INFO, m_loggerSupport.logbackToScoutLevel(Level.INFO));
    assertEquals(LogLevel.WARN, m_loggerSupport.logbackToScoutLevel(Level.WARN));
    assertEquals(LogLevel.ERROR, m_loggerSupport.logbackToScoutLevel(Level.ERROR));
    assertEquals(LogLevel.OFF, m_loggerSupport.logbackToScoutLevel(Level.OFF));
  }

  @Test
  public void testGetLogLevelUsingLoggerName() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
    setLogbackLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingLoggerClass() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_CLASS));
    setLogbackLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingSlf4jLoggerInstance() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER));
    setLogbackLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testSetLogLevelUsingLoggerName() {
    assertNull(getLogbackLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_NAME, LogLevel.INFO);
    assertEquals(Level.INFO, getLogbackLevel());
  }

  @Test
  public void testSetLogLevelUsingLoggerClass() {
    assertNull(getLogbackLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_CLASS, LogLevel.INFO);
    assertEquals(Level.INFO, getLogbackLevel());
  }

  @Test
  public void testSetLogLevelUsingSlf4jLoggerInstance() {
    assertNull(getLogbackLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER, LogLevel.INFO);
    assertEquals(Level.INFO, getLogbackLevel());
  }

  private void setLogbackLevel(Level level) {
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TEST_LOGGER_NAME)).setLevel(level);
  }

  private Level getLogbackLevel() {
    return ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TEST_LOGGER_NAME)).getLevel();
  }
}
