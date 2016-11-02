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
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CompareUtility}
 *
 * @since 3.8.1
 */
@SuppressWarnings("deprecation")
public class CompareUtilityTest {

  private static final String TEST_STRING = "test";
  private static final String FOO_STRING = "foo";
  private static final String BAR_STRING = "bar";

  private static final long TEST_NUMBER = 12L;
  private static final long TEST_MILLIS = 1398071807000L;

  private Timestamp m_testTimestamp;
  private Date m_testDate;

  @Before
  public void setup() {
    m_testTimestamp = new Timestamp(TEST_MILLIS);
    m_testDate = new Date(TEST_MILLIS);
  }

  @Test
  public void testIsOneOf() {
    assertFalse(CompareUtility.isOneOf(TEST_STRING, (Object[]) null));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, new Object[0]));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, BAR_STRING));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, TEST_NUMBER));
    assertFalse(CompareUtility.isOneOf(null, FOO_STRING, TEST_NUMBER));
    assertFalse(CompareUtility.isOneOf(null, (Object[]) null));
    assertTrue(CompareUtility.isOneOf(TEST_STRING, TEST_STRING, TEST_NUMBER));
    assertTrue(CompareUtility.isOneOf(null, TEST_STRING, TEST_NUMBER, null));
    assertTrue(CompareUtility.isOneOf(null, (Object) null));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.CompareUtility#equals(T, T) CompareUtility#equals(T, T)} with
   * respect to {@link java.util.Date} and {@link java.sql.Timestamp}. Comparison with null.
   */
  @Test
  public void testTimestampDateEquality_NullComparison() {
    assertFalse(CompareUtility.equals(m_testDate, null));
    assertFalse(CompareUtility.equals(null, m_testDate));
    assertFalse(CompareUtility.equals(m_testTimestamp, null));
    assertFalse(CompareUtility.equals(null, m_testTimestamp));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.CompareUtility#equals(T, T) CompareUtility#equals(T, T)} with
   * respect to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison between _equal_ dates of different types.
   */
  @Test
  public void testTimestampDateEquality_DifferentTypes_EqualDates() {
    assertTrue(CompareUtility.equals(m_testDate, m_testDate));
    assertTrue(CompareUtility.equals(m_testTimestamp, m_testTimestamp));
    assertTrue(CompareUtility.equals(m_testDate, m_testTimestamp));
    assertTrue(CompareUtility.equals(m_testTimestamp, m_testDate)); // <-- this fails with default Timestamp.equals()
    assertEquals(m_testDate.hashCode(), m_testTimestamp.hashCode());
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.CompareUtility#equals(T, T) CompareUtility#equals(T, T)} with
   * respect to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison between _unequal_ dates of different types
   */
  @Test
  public void testTimestampDateEquality_DifferentTypes_UnequalDates() {
    Date testDate2 = new Date(TEST_MILLIS + 1);
    assertFalse(CompareUtility.equals(m_testDate, testDate2));
    assertFalse(CompareUtility.equals(m_testTimestamp, testDate2));
    assertFalse(CompareUtility.equals(testDate2, m_testDate));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.CompareUtility#equals(T, T) CompareUtility#equals(T, T)} with
   * respect to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison of objects of non-related types
   */
  @Test
  public void testTimestampDateEquality_NonRelatedTypes() {
    Object o = new Object();
    assertFalse(CompareUtility.equals(m_testTimestamp, TEST_STRING));
    assertFalse(CompareUtility.equals(TEST_STRING, m_testTimestamp));
    assertFalse(CompareUtility.equals(m_testTimestamp, o));
    assertFalse(CompareUtility.equals(o, m_testTimestamp));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.CompareUtility#equals(T, T) CompareUtility#equals(T, T)} with
   * respect to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison of non-Date/Timestamp objects
   */
  @Test
  public void testEquality_Strings() {
    String s = TEST_STRING;
    String s2 = TEST_STRING;
    String s3 = "another string";

    assertTrue(CompareUtility.equals(s, s2));
    assertTrue(CompareUtility.equals(s2, s));
    assertFalse(CompareUtility.equals(s, s3));
    assertFalse(CompareUtility.equals(s3, s));
  }

}
