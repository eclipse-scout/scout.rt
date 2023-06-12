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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ConfigurationEvent.EventType;
import ch.qos.logback.core.spi.ConfigurationEventListener;

/**
 * @since 5.2
 */
public class LogbackLoggerSupportTest extends AbstractLoggerSupportTest {

  private static final Class<?> TEST_LOGGER_CLASS = LogbackLoggerSupportTest.class;
  private static final String TEST_LOGGER_NAME = TEST_LOGGER_CLASS.getName();
  private static final Logger TEST_LOGGER = LoggerFactory.getLogger(TEST_LOGGER_CLASS);

  private LogbackLoggerSupport m_loggerSupport;
  private LoggerContext m_loggerContextMock = mock(LoggerContext.class);

  @Before
  public void before() {
    m_loggerContextMock = mock(LoggerContext.class);
    m_loggerSupport = new LogbackLoggerSupport(m_loggerContextMock);
  }

  @After
  public void after() {
    // reset log level
    setLogbackLevel(null);
  }

  @Test
  public void testScoutToLogbackLevel() {
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
  public void testLogbackToScoutLevel() {
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

  @Test
  public void testConfigurationEndedListenerRegistration() {
    verify(m_loggerContextMock, times(1)).addConfigurationEventListener(any(ConfigurationEventListener.class));
  }

  @Test
  public void testChangeTracking() {
    ArgumentCaptor<ConfigurationEventListener> captor = ArgumentCaptor.forClass(ConfigurationEventListener.class);
    verify(m_loggerContextMock, times(1)).addConfigurationEventListener(captor.capture());
    ConfigurationEventListener listener = captor.getValue();

    // set level to ERROR
    assertNull(getLogbackLevel());
    m_loggerSupport.setLogLevel(TEST_LOGGER_CLASS, LogLevel.ERROR);
    assertEquals(getLogbackLevel(), Level.ERROR);

    // now set level again (w/o logger support otherwise this would be tracked as change)
    ((ch.qos.logback.classic.Logger) TEST_LOGGER).setLevel(null);
    assertNull(getLogbackLevel());

    // trigger event listener
    ConfigurationEvent event = mock(ConfigurationEvent.class);
    when(event.getEventType()).thenReturn(EventType.CONFIGURATION_ENDED);
    listener.listen(event);

    // expect previous change to be restored again
    assertEquals(getLogbackLevel(), Level.ERROR);
  }

  private void setLogbackLevel(Level level) {
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TEST_LOGGER_NAME)).setLevel(level);
  }

  private Level getLogbackLevel() {
    return ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TEST_LOGGER_NAME)).getLevel();
  }

  @Override
  protected LogbackLoggerSupport getLoggerSupport() {
    return m_loggerSupport;
  }

  @Override
  protected String getTestLoggerName() {
    return TEST_LOGGER_NAME;
  }

  @Override
  protected LogLevel getTestLoggerLevel() {
    return m_loggerSupport.logbackToScoutLevel(getLogbackLevel());
  }
}
