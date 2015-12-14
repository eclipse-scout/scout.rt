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

import java.util.logging.Level;

import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.2
 */
public class JulLoggerSupportTest {

  private static final Class<?> TEST_LOGGER_CLASS = JulLoggerSupportTest.class;
  private static final String TEST_LOGGER_NAME = TEST_LOGGER_CLASS.getName();
  private static final Logger TEST_LOGGER = LoggerFactory.getLogger(TEST_LOGGER_CLASS);

  private JulLoggerSupport m_loggerSupport;

  @Before
  public void before() {
    m_loggerSupport = new JulLoggerSupport();
  }

  @After
  public void after() {
    // reset log level
    setJulLevel(null);
  }

  @Test
  public void testScoutToJulLevel() {
    assertNull(m_loggerSupport.scoutToJulLevel(null));
    assertEquals(Level.ALL, m_loggerSupport.scoutToJulLevel(LogLevel.ALL));
    assertEquals(Level.FINEST, m_loggerSupport.scoutToJulLevel(LogLevel.TRACE));
    assertEquals(Level.FINE, m_loggerSupport.scoutToJulLevel(LogLevel.DEBUG));
    assertEquals(Level.INFO, m_loggerSupport.scoutToJulLevel(LogLevel.INFO));
    assertEquals(Level.WARNING, m_loggerSupport.scoutToJulLevel(LogLevel.WARN));
    assertEquals(Level.SEVERE, m_loggerSupport.scoutToJulLevel(LogLevel.ERROR));
    assertEquals(Level.OFF, m_loggerSupport.scoutToJulLevel(LogLevel.OFF));
  }

  @Test
  public void testJulToScoutLevel() {
    assertNull(m_loggerSupport.scoutToJulLevel(null));
    assertEquals(LogLevel.ALL, m_loggerSupport.julToScoutLevel(Level.ALL));
    assertEquals(LogLevel.TRACE, m_loggerSupport.julToScoutLevel(Level.FINEST));
    assertEquals(LogLevel.DEBUG, m_loggerSupport.julToScoutLevel(Level.FINER));
    assertEquals(LogLevel.DEBUG, m_loggerSupport.julToScoutLevel(Level.FINE));
    assertEquals(LogLevel.INFO, m_loggerSupport.julToScoutLevel(Level.CONFIG));
    assertEquals(LogLevel.INFO, m_loggerSupport.julToScoutLevel(Level.INFO));
    assertEquals(LogLevel.WARN, m_loggerSupport.julToScoutLevel(Level.WARNING));
    assertEquals(LogLevel.ERROR, m_loggerSupport.julToScoutLevel(Level.SEVERE));
    assertEquals(LogLevel.OFF, m_loggerSupport.julToScoutLevel(Level.OFF));
  }

  @Test
  public void testGetLogLevelUsingLoggerName() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
    setJulLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingLoggerClass() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_CLASS));
    setJulLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingSlf4jLoggerInstance() {
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER));
    setJulLevel(Level.INFO);
    assertEquals(LogLevel.INFO, m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
  }

  @Test
  public void testSetLogLevelUsingLoggerName() {
    assertNull(getJulLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_NAME, LogLevel.INFO);
    assertEquals(Level.INFO, getJulLevel());
  }

  @Test
  public void testSetLogLevelUsingLoggerClass() {
    assertNull(getJulLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_CLASS, LogLevel.INFO);
    assertEquals(Level.INFO, getJulLevel());
  }

  @Test
  public void testSetLogLevelUsingSlf4jLoggerInstance() {
    assertNull(getJulLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER, LogLevel.INFO);
    assertEquals(Level.INFO, getJulLevel());
  }

  @Test(expected = NullPointerException.class)
  public void testGetJulLoggerNull() {
    m_loggerSupport.getJulLogger(null);
  }

  @Test
  public void testGetJulRootLogger() {
    java.util.logging.Logger julLogger = m_loggerSupport.getJulLogger(Logger.ROOT_LOGGER_NAME);
    assertNotNull(julLogger);
    assertEquals("", julLogger.getName());
  }

  private void setJulLevel(Level level) {
    java.util.logging.Logger.getLogger(TEST_LOGGER_NAME).setLevel(level);
  }

  private Level getJulLevel() {
    return java.util.logging.Logger.getLogger(TEST_LOGGER_NAME).getLevel();
  }
}
