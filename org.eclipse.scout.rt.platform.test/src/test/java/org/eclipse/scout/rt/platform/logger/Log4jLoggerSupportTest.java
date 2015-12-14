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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.2
 */
public class Log4jLoggerSupportTest {

  private static final Class<?> TEST_LOGGER_CLASS = Log4jLoggerSupportTest.class;
  private static final String TEST_LOGGER_NAME = TEST_LOGGER_CLASS.getName();
  private static final Logger TEST_LOGGER = LoggerFactory.getLogger(TEST_LOGGER_CLASS);

  private Log4jLoggerSupport m_loggerSupport;

  @Before
  public void before() {
    m_loggerSupport = new Log4jLoggerSupport();
  }

  @After
  public void after() {
    // reset log level
    setLog4jLevel(null);
  }

  @Test
  public void testScoutToLog4jLevel() {
    assertNull(m_loggerSupport.scoutToLog4jLevel(null));
    assertEquals(Level.ALL, m_loggerSupport.scoutToLog4jLevel(LogLevel.ALL));
    assertEquals(Level.TRACE, m_loggerSupport.scoutToLog4jLevel(LogLevel.TRACE));
    assertEquals(Level.DEBUG, m_loggerSupport.scoutToLog4jLevel(LogLevel.DEBUG));
    assertEquals(Level.INFO, m_loggerSupport.scoutToLog4jLevel(LogLevel.INFO));
    assertEquals(Level.WARN, m_loggerSupport.scoutToLog4jLevel(LogLevel.WARN));
    assertEquals(Level.ERROR, m_loggerSupport.scoutToLog4jLevel(LogLevel.ERROR));
    assertEquals(Level.OFF, m_loggerSupport.scoutToLog4jLevel(LogLevel.OFF));
  }

  @Test
  public void testLog4jToScoutLevel() {
    assertNull(m_loggerSupport.log4jToScoutLevel(null));
    assertEquals(LogLevel.ALL, m_loggerSupport.log4jToScoutLevel(Level.ALL));
    assertEquals(LogLevel.TRACE, m_loggerSupport.log4jToScoutLevel(Level.TRACE));
    assertEquals(LogLevel.DEBUG, m_loggerSupport.log4jToScoutLevel(Level.DEBUG));
    assertEquals(LogLevel.INFO, m_loggerSupport.log4jToScoutLevel(Level.INFO));
    assertEquals(LogLevel.WARN, m_loggerSupport.log4jToScoutLevel(Level.WARN));
    assertEquals(LogLevel.ERROR, m_loggerSupport.log4jToScoutLevel(Level.ERROR));
    assertEquals(LogLevel.ERROR, m_loggerSupport.log4jToScoutLevel(Level.FATAL));
    assertEquals(LogLevel.OFF, m_loggerSupport.log4jToScoutLevel(Level.OFF));
  }

  @Test
  public void testGetLogLevelUsingLoggerName() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
    setLog4jLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingLoggerClass() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_CLASS));
    setLog4jLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingSlf4jLoggerInstance() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER));
    setLog4jLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testSetLogLevelUsingLoggerName() {
    assertNull(getLog4jLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_NAME, LogLevel.INFO);
    assertEquals(Level.INFO, getLog4jLevel());
  }

  @Test
  public void testSetLogLevelUsingLoggerClass() {
    assertNull(getLog4jLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_CLASS, LogLevel.INFO);
    assertEquals(Level.INFO, getLog4jLevel());
  }

  @Test
  public void testSetLogLevelUsingSlf4jLoggerInstance() {
    assertNull(getLog4jLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER, LogLevel.INFO);
    assertEquals(Level.INFO, getLog4jLevel());
  }

  @Test(expected = NullPointerException.class)
  public void testGetLog4jLoggerNull() {
    m_loggerSupport.getLog4jLogger(null);
  }

  @Test
  public void testGetLog4jRootLogger() {
    org.apache.log4j.Logger log4jLogger = m_loggerSupport.getLog4jLogger(Logger.ROOT_LOGGER_NAME);
    assertNotNull(log4jLogger);
    assertEquals("root", log4jLogger.getName());
  }

  private void setLog4jLevel(Level level) {
    LogManager.getLogger(TEST_LOGGER_NAME).setLevel(level);
  }

  private Level getLog4jLevel() {
    return LogManager.getLogger(TEST_LOGGER_NAME).getLevel();
  }
}
