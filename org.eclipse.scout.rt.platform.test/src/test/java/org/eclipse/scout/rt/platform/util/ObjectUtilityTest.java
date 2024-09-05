/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

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
  private static final long TEST_MILLIS = 1398071807123L;
  private static final int TEST_NANOS = 123456789; // same millis as TEST_MILLIS

  private static Timestamp m_testTimestamp;
  private static Timestamp m_testTimestampPlusNanos;
  private static Date m_testDate;

  @BeforeClass
  public static void beforeClass() {
    m_testTimestamp = new Timestamp(TEST_MILLIS);
    m_testTimestampPlusNanos = new Timestamp(TEST_MILLIS);
    m_testTimestampPlusNanos.setNanos(TEST_NANOS);
    m_testDate = new Date(TEST_MILLIS);
  }

  @Test
  public void testNvl() {
    testNvlInternal(ObjectUtility::nvl);
  }

  @Test
  public void testNvlOpt() {
    testNvlInternal((o1, o2) -> ObjectUtility.nvlOpt(o1, () -> o2));
  }

  protected <T> void testNvlInternal(BiFunction<Object, Object, Object> nvlFunction) {
    Object o1 = "foo";
    Object o2 = 1234;
    assertSame(o1, nvlFunction.apply(o1, o2));
    assertSame(o2, nvlFunction.apply(o2, o1));
    assertSame(o1, nvlFunction.apply(null, o1));
    assertNull(nvlFunction.apply(null, null));
  }

  @Test
  public void testIsOneOf() {
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, (Object[]) null));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, new Object[0]));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, FOO_STRING, BAR_STRING));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, FOO_STRING, TEST_NUMBER));
    assertFalse(ObjectUtility.isOneOf(null, FOO_STRING, TEST_NUMBER));
    assertFalse(ObjectUtility.isOneOf(null, (Object[]) null));
    assertFalse(ObjectUtility.isOneOf(TEST_STRING, Collections.emptyList()));
    assertTrue(ObjectUtility.isOneOf(null, Collections.singletonList(null)));
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

  @Test
  public void testTimestampDateEquality_DifferentTypes_UnequalDatesNanos() {
    assertFalse(ObjectUtility.equals(m_testDate, m_testTimestampPlusNanos));
    assertFalse(ObjectUtility.equals(m_testTimestampPlusNanos, m_testDate));
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
    assertTrue(ObjectUtility.compareTo(m_testTimestamp, m_testTimestampPlusNanos) < 0);
    assertTrue(ObjectUtility.compareTo(m_testTimestampPlusNanos, m_testTimestamp) > 0);
  }

  @Test
  public void testTimestampDateComparison_DifferentTypes_EqualDates() {
    assertEquals(0, ObjectUtility.compareTo(m_testTimestamp, m_testDate));
    assertEquals(0, ObjectUtility.compareTo(m_testDate, m_testTimestamp)); // <-- this fails with default Date.compareTo() on Java 8 (JDK-8135055)
  }

  @Test
  public void testTimestampDateComparison_DifferentTypes_UnequalDatesNanos() {
    assertTrue(ObjectUtility.compareTo(m_testDate, m_testTimestampPlusNanos) < 0);
    assertTrue(ObjectUtility.compareTo(m_testTimestampPlusNanos, m_testDate) > 0);
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

  @Test
  public void testHasValue() {
    assertFalse(ObjectUtility.hasValue(null));
    assertTrue(ObjectUtility.hasValue(new Object()));

    // common primitives
    assertTrue(ObjectUtility.hasValue((byte) 0));
    assertTrue(ObjectUtility.hasValue(' '));
    assertTrue(ObjectUtility.hasValue(0));
    assertTrue(ObjectUtility.hasValue(1));
    assertTrue(ObjectUtility.hasValue(-10));
    assertTrue(ObjectUtility.hasValue(Integer.MAX_VALUE));
    assertTrue(ObjectUtility.hasValue(Integer.MIN_VALUE));
    assertTrue(ObjectUtility.hasValue(Float.MAX_VALUE));
    assertTrue(ObjectUtility.hasValue(Float.MIN_VALUE));
    assertTrue(ObjectUtility.hasValue(0.0));
    assertTrue(ObjectUtility.hasValue(1.0));
    assertTrue(ObjectUtility.hasValue(true));
    assertTrue(ObjectUtility.hasValue(false));

    // optionals
    assertFalse(ObjectUtility.hasValue(Optional.empty()));
    assertTrue(ObjectUtility.hasValue(Optional.of(0)));
    assertTrue(ObjectUtility.hasValue(Optional.of(true)));
    assertTrue(ObjectUtility.hasValue(Optional.of("")));

    // strings
    assertFalse(ObjectUtility.hasValue(""));
    assertFalse(ObjectUtility.hasValue(" "));
    assertTrue(ObjectUtility.hasValue("abc"));

    // collections
    assertFalse(ObjectUtility.hasValue(List.of()));
    assertTrue(ObjectUtility.hasValue(List.of(1)));
    ArrayList<Object> listWithNullValue = new ArrayList<>();
    listWithNullValue.add(null);
    assertTrue(ObjectUtility.hasValue(listWithNullValue));
    assertTrue(ObjectUtility.hasValue(List.of("")));

    // maps
    assertFalse(ObjectUtility.hasValue(Map.of()));
    assertTrue(ObjectUtility.hasValue(Map.of(1, 2)));
    HashMap<Object, Object> mapWithNullValue = new HashMap<>();
    mapWithNullValue.put(null, null);
    assertTrue(ObjectUtility.hasValue(mapWithNullValue));
    assertTrue(ObjectUtility.hasValue(Map.of("", "")));

    // arrays
    assertFalse(ObjectUtility.hasValue(new int[]{}));
    assertTrue(ObjectUtility.hasValue(new int[]{1, 2}));
    assertTrue(ObjectUtility.hasValue(new Object[]{null}));
    assertTrue(ObjectUtility.hasValue(new String[]{""}));
  }
}
