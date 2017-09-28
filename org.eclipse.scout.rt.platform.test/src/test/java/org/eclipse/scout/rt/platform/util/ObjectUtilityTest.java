/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link ObjectUtility}
 */
public class ObjectUtilityTest {

  private static final String TEST_STRING = "test";
  private static final String FOO_STRING = "foo";
  private static final String BAR_STRING = "bar";

  private static final long TEST_NUMBER = 12L;
  private static final long TEST_MILLIS = 1398071807000L;

  private static Timestamp m_testTimestamp;
  private static Date m_testDate;

  @BeforeClass
  public static void beforeClass() {
    m_testTimestamp = new Timestamp(TEST_MILLIS);
    m_testDate = new Date(TEST_MILLIS);
  }

  @Test
  public void testNvl() {
    Object o1 = "foo";
    Object o2 = 1234;
    assertSame(o1, ObjectUtility.nvl(o1, o2));
    assertSame(o2, ObjectUtility.nvl(o2, o1));
    assertSame(o1, ObjectUtility.nvl(null, o1));
    assertNull(ObjectUtility.nvl(null, null));
  }

  @Test
  public void testIsOneOf() {
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, (Object[]) null));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, new Object[0]));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, FOO_STRING, BAR_STRING));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, FOO_STRING, TEST_NUMBER));
    assertFalse(ObjectUtility.isOneOf(null, FOO_STRING, TEST_NUMBER));
    assertFalse(ObjectUtility.isOneOf(null, (Object[]) null));
    assertTrue(ObjectUtility.isOneOf(TEST_STRING, TEST_STRING, TEST_NUMBER));
    assertTrue(ObjectUtility.isOneOf(null, TEST_STRING, TEST_NUMBER, null));
    assertTrue(ObjectUtility.isOneOf(null, (Object) null));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.ObjectUtility#equals(T, T) ObjectUtility#equals(T, T)} with respect
   * to {@link java.util.Date} and {@link java.sql.Timestamp}. Comparison with null.
   */
  @Test
  public void testTimestampDateEquality_NullComparison() {
    assertFalse(ObjectUtility.equals(m_testDate, null));
    assertFalse(ObjectUtility.equals(null, m_testDate));
    assertFalse(ObjectUtility.equals(m_testTimestamp, null));
    assertFalse(ObjectUtility.equals(null, m_testTimestamp));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.ObjectUtility#equals(T, T) ObjectUtility#equals(T, T)} with respect
   * to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison between _equal_ dates of different types.
   */
  @Test
  public void testTimestampDateEquality_DifferentTypes_EqualDates() {
    assertTrue(ObjectUtility.equals(m_testDate, m_testDate));
    assertTrue(ObjectUtility.equals(m_testTimestamp, m_testTimestamp));
    assertTrue(ObjectUtility.equals(m_testDate, m_testTimestamp));
    assertTrue(ObjectUtility.equals(m_testTimestamp, m_testDate)); // <-- this fails with default Timestamp.equals()
    assertEquals(m_testDate.hashCode(), m_testTimestamp.hashCode());
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.ObjectUtility#equals(T, T) ObjectUtility#equals(T, T)} with respect
   * to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison between _unequal_ dates of different types
   */
  @Test
  public void testTimestampDateEquality_DifferentTypes_UnequalDates() {
    Date testDate2 = new Date(TEST_MILLIS + 1);
    assertFalse(ObjectUtility.equals(m_testDate, testDate2));
    assertFalse(ObjectUtility.equals(m_testTimestamp, testDate2));
    assertFalse(ObjectUtility.equals(testDate2, m_testDate));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.ObjectUtility#equals(T, T) ObjectUtility#equals(T, T)} with respect
   * to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison of objects of non-related types
   */
  @Test
  public void testTimestampDateEquality_NonRelatedTypes() {
    Object o = new Object();
    assertFalse(ObjectUtility.equals(m_testTimestamp, TEST_STRING));
    assertFalse(ObjectUtility.equals(TEST_STRING, m_testTimestamp));
    assertFalse(ObjectUtility.equals(m_testTimestamp, o));
    assertFalse(ObjectUtility.equals(o, m_testTimestamp));
  }

  /**
   * Tests {@link org.eclipse.scout.rt.platform.util.ObjectUtility#equals(T, T) ObjectUtility#equals(T, T)} with respect
   * to {@link java.util.Date} and {@link java.sql.Timestamp}. <br>
   * Test comparison of non-Date/Timestamp objects
   */
  @Test
  public void testEquality_Strings() {
    String s = TEST_STRING;
    String s2 = TEST_STRING;
    String s3 = "another string";

    assertTrue(ObjectUtility.equals(s, s2));
    assertTrue(ObjectUtility.equals(s2, s));
    assertFalse(ObjectUtility.equals(s, s3));
    assertFalse(ObjectUtility.equals(s3, s));
  }

  @Test
  public void testEquality_Arrays() {
    int[] arr1 = new int[]{1, 2, 3};
    int[] arr2 = new int[]{2, 3, 4};
    int[] arr3 = new int[]{1, 2, 3};
    int[] arr4 = new int[]{3, 4};

    assertTrue(ObjectUtility.equals(arr1, arr3));
    assertFalse(ObjectUtility.equals(arr1, arr2));
    assertFalse(ObjectUtility.equals(arr2, arr3));
    assertFalse(ObjectUtility.equals(arr1, arr4));
    assertFalse(ObjectUtility.equals(arr4, arr1));
    assertFalse(ObjectUtility.equals(arr1, ""));
  }

  @Test
  public void testNotEquals() {
    assertTrue(ObjectUtility.notEquals("a", "b"));
    assertFalse(ObjectUtility.notEquals("a", "a"));
  }

  @Test
  public void testCompareTo() {
    assertEquals(0, ObjectUtility.compareTo(null, null));
    assertEquals(-1, ObjectUtility.compareTo(null, "b"));
    assertEquals(1, ObjectUtility.compareTo("a", null));
    assertTrue(ObjectUtility.compareTo("a", "b") < 0);
  }

  @Test
  public void testToString() {
    assertEquals("value", ObjectUtility.toString("value"));
    assertEquals("5", ObjectUtility.toString(Integer.valueOf(5)));
    assertNull(ObjectUtility.toString(null));

    Object o = new Object() {
      @Override
      public String toString() {
        return "object";
      }
    };
    assertEquals("object", ObjectUtility.toString(o));
  }
}
