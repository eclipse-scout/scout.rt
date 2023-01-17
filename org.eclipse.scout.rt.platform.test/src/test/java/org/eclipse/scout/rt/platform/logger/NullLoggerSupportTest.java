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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.2
 */
public class NullLoggerSupportTest {

  private static final Class<?> TEST_LOGGER_CLASS = NullLoggerSupportTest.class;
  private static final String TEST_LOGGER_NAME = TEST_LOGGER_CLASS.getName();
  private static final Logger TEST_LOGGER = LoggerFactory.getLogger(TEST_LOGGER_CLASS);

  private NullLoggerSupport m_loggerSupport;

  @Before
  public void before() {
    m_loggerSupport = new NullLoggerSupport("testCase");
  }

  @Test
  public void testGetLogLevelUsingLoggerName() {
    assertFalse(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_NAME));
    assertTrue(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingLoggerClass() {
    assertFalse(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER_CLASS));
    assertTrue(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
  }

  @Test
  public void testGetLogLevelUsingSlf4jLoggerInstance() {
    assertFalse(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
    assertNull(m_loggerSupport.getLogLevel(TEST_LOGGER));
    assertTrue(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
  }

  @Test
  public void testSetLogLevelUsingLoggerName() {
    assertFalse(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
    m_loggerSupport.setLogLevel(TEST_LOGGER_NAME, null);
    assertTrue(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
  }

  @Test
  public void testSetLogLevelUsingLoggerClass() {
    assertFalse(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
    m_loggerSupport.setLogLevel(TEST_LOGGER_CLASS, null);
    assertTrue(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
  }

  @Test
  public void testSetLogLevelUsingSlf4jLoggerInstance() {
    assertFalse(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
    m_loggerSupport.setLogLevel(TEST_LOGGER, null);
    assertTrue(m_loggerSupport.getQueriedLoggers().containsKey(TEST_LOGGER_NAME));
  }
}
