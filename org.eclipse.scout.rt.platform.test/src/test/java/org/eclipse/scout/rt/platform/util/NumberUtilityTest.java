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

import static org.eclipse.scout.rt.platform.util.NumberUtility.divideAndCeil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.Test;

public class NumberUtilityTest {

  private static final double EPSILON = 0.01;

  private static Number[] NUMBER_ARRAY = new Number[]{Long.valueOf(100),
      new BigInteger("1000000000000000000000000000000000000000000000000000000"),
      BigDecimal.ONE,
      BigDecimal.ZERO,
      Double.valueOf(2.225d),
      null,
      Float.NaN,
      1f,
      0.1d,
      -4.1f,
      1,
      2L,
      Float.NEGATIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Integer.valueOf(45)};

  /**
   * Test method for {@link org.eclipse.scout.rt.platform.util.NumberUtility#numberToBigDecimal(java.lang.Number)}.
   */
  @Test
  public void testToBigDecimalNumber() {
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25d), NumberUtility.numberToBigDecimal(Integer.valueOf(25)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25d), NumberUtility.numberToBigDecimal(Long.valueOf(25)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25.987d), NumberUtility.numberToBigDecimal(Float.valueOf(25.987f)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25.987d), NumberUtility.numberToBigDecimal(Double.valueOf(25.987d)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-25d), NumberUtility.numberToBigDecimal(Double.valueOf(-25d)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-25d), NumberUtility.numberToBigDecimal(BigDecimal.valueOf(-25d)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-25d), NumberUtility.numberToBigDecimal(BigInteger.valueOf(-25)));

    assertNull(NumberUtility.numberToBigDecimal(Double.valueOf(Double.NEGATIVE_INFINITY)));
    assertNull(NumberUtility.numberToBigDecimal(Double.valueOf(Double.POSITIVE_INFINITY)));
    assertNull(NumberUtility.numberToBigDecimal(Double.valueOf(Double.NaN)));
    assertNull(NumberUtility.numberToBigDecimal(Float.valueOf(Float.NEGATIVE_INFINITY)));
    assertNull(NumberUtility.numberToBigDecimal(Float.valueOf(Float.POSITIVE_INFINITY)));
    assertNull(NumberUtility.numberToBigDecimal(Float.valueOf(Float.NaN)));

    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(Integer.MAX_VALUE), NumberUtility.numberToBigDecimal(Integer.MAX_VALUE));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(Integer.MIN_VALUE), NumberUtility.numberToBigDecimal(Integer.MIN_VALUE));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(Long.MAX_VALUE), NumberUtility.numberToBigDecimal(Long.MAX_VALUE));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(Long.MIN_VALUE), NumberUtility.numberToBigDecimal(Long.MIN_VALUE));
    ScoutAssert.assertComparableEquals(new BigDecimal("999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"),
        NumberUtility.numberToBigDecimal(new BigInteger("999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")));
    ScoutAssert.assertComparableEquals(new BigDecimal("999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"),
        NumberUtility.numberToBigDecimal(new BigInteger("999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(Double.MAX_VALUE), NumberUtility.numberToBigDecimal(BigDecimal.valueOf(Double.MAX_VALUE)));
  }

  @Test
  public void testToDouble() {
    assertNull(NumberUtility.toDouble(null));

    Integer intValue = Integer.valueOf(42);
    assertEquals(Double.valueOf(42), NumberUtility.toDouble(intValue));

    Float floatValue = 42.0001f;
    assertEquals(Double.valueOf(42.0001), NumberUtility.toDouble(floatValue));
  }

  @Test
  public void testDivideAndCeil() {
    assertEquals(2, divideAndCeil(8, 4));
    assertEquals(3, divideAndCeil(9, 4));
    assertEquals(3, divideAndCeil(10, 4));
    assertEquals(3, divideAndCeil(11, 4));
    assertEquals(3, divideAndCeil(12, 4));
    assertEquals(4, divideAndCeil(13, 4));
    assertEquals(12, divideAndCeil(12, 1));
  }

  @Test
  public void testSumNumbersVararg() {
    assertEquals(new BigDecimal("1000000000000000000000000000000000000000000000000000148.225"),
        NumberUtility.sum(Long.valueOf(100),
            new BigInteger("1000000000000000000000000000000000000000000000000000000"),
            BigDecimal.ONE,
            BigDecimal.ZERO,
            Double.valueOf(2.225d),
            null,
            Float.NaN,
            Float.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Integer.valueOf(45)));
  }

  @Test
  public void testSumNumbersIntAutoBox() {
    assertEquals(new BigDecimal("503"),
        NumberUtility.sum(1, 2, 500));
  }

  @Test
  public void testSumNumbersFloatDoubleAutoBox() {
    assertEquals(new BigDecimal("1.234456"),
        NumberUtility.sum(1.111f, 0.123456d));
  }

  @Test
  public void testSumNumbersArray() {

    assertEquals(new BigDecimal("1000000000000000000000000000000000000000000000000000148.225"),
        NumberUtility.sum(NUMBER_ARRAY));
  }

  @Test
  public void testSumNumbersCollection() {
    List<Number> numberCollection = Arrays.asList(NUMBER_ARRAY);

    assertEquals(new BigDecimal("1000000000000000000000000000000000000000000000000000148.225"),
        NumberUtility.sum(numberCollection));
  }

  /**
   * Test of method {@link NumberUtility#median(double...)}
   */
  @Test
  public void testMedian() {
    // test behavior on null argument
    double d = NumberUtility.median(null);
    assertEquals(0, d, EPSILON);
    // test behavior on zero length argument
    double[] darray = {};
    d = NumberUtility.median(darray);
    assertEquals(0, d, EPSILON);
    // test behavior on odd length array
    d = NumberUtility.median(10.0);
    assertEquals(10.0, d, EPSILON);
    d = NumberUtility.median(10.0, 15.0, 20.0);
    assertEquals(15.0, d, EPSILON);
    // test behavior on even length array
    d = NumberUtility.median(10.0, 20.0);
    assertEquals(20.0, d, EPSILON);
    d = NumberUtility.median(10.0, 20.0, 30.0, 40.0);
    assertEquals(30.0, d, EPSILON);
  }

  @Test
  public void testIsDoubleDifferent() {
    assertTrue(NumberUtility.isDoubleDifferent(1.1113d, 1.1115d, 0.0001d));
    assertTrue(NumberUtility.isDoubleDifferent(1.0d, 2.0d, 0.9d));
    assertFalse(NumberUtility.isDoubleDifferent(1.111d, 1.112d, 0.01d));
    assertFalse(NumberUtility.isDoubleDifferent(-0.0d, 0.0d, 0.000000001d));
    assertFalse(NumberUtility.isDoubleDifferent(-0.0d, 0.0d, 1d));
    assertFalse(NumberUtility.isDoubleDifferent(-0.0d, 0.0d, 0d));

    // min/max values
    assertTrue(NumberUtility.isDoubleDifferent(-Double.MAX_VALUE, Double.MAX_VALUE, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.MAX_VALUE, Double.MIN_VALUE, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.MIN_VALUE, Double.MAX_VALUE, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.MAX_VALUE, -Double.MAX_VALUE, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.MIN_VALUE, -Double.MIN_VALUE, 0d));
    assertFalse(NumberUtility.isDoubleDifferent(Double.MAX_VALUE, Double.MAX_VALUE, 10000d));
    assertFalse(NumberUtility.isDoubleDifferent(Double.MIN_VALUE, Double.MIN_VALUE, 10000d));
    assertFalse(NumberUtility.isDoubleDifferent(-Double.MAX_VALUE, -Double.MAX_VALUE, 10000d));
    assertFalse(NumberUtility.isDoubleDifferent(-Double.MIN_VALUE, -Double.MIN_VALUE, 10000d));

    // infinity comparisons
    assertFalse(NumberUtility.isDoubleDifferent(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 10000d));
    assertFalse(NumberUtility.isDoubleDifferent(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.NEGATIVE_INFINITY, 100d, 10000d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.POSITIVE_INFINITY, 100d, 10000d));

    // NaN comparisons
    assertTrue(NumberUtility.isDoubleDifferent(1.0d, Double.NaN, 1d));
    assertTrue(NumberUtility.isDoubleDifferent(Double.NaN, 1.0d, 1d));
    assertFalse(NumberUtility.isDoubleDifferent(Double.NaN, Double.NaN, 1d));
    assertFalse(NumberUtility.isDoubleDifferent(Float.NaN, Float.NaN, Float.POSITIVE_INFINITY));
    assertFalse(NumberUtility.isDoubleDifferent(Double.NaN, Double.NaN, Double.POSITIVE_INFINITY));
    assertFalse(NumberUtility.isDoubleDifferent(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
  }

  @Test
  public void testParseEmptyString() {
    assertEquals(0d, NumberUtility.parseDouble(""), 0.00001d);
    assertEquals(0l, NumberUtility.parseLong(""));
    assertEquals(0, NumberUtility.parseInt(""));
  }

  @Test
  public void testIsValidDouble() {
    assertFalse(NumberUtility.isValidDouble("ABC", ",", "."));
    assertTrue(NumberUtility.isValidDouble("123", ",", "."));
    assertTrue(NumberUtility.isValidDouble("+123", ",", "."));
    assertTrue(NumberUtility.isValidDouble("-123", ",", "."));
    assertTrue(NumberUtility.isValidDouble("123,45", ",", "."));
    assertTrue(NumberUtility.isValidDouble("123.456,789", ",", "."));
    assertTrue(NumberUtility.isValidDouble("123,456.789", ",", "."));
    assertTrue(NumberUtility.isValidDouble("-123.456,789", ",", "."));
    assertTrue(NumberUtility.isValidDouble("+123.456,789", ",", "."));
    assertFalse(NumberUtility.isValidDouble("+-123", ",", "."));
    assertFalse(NumberUtility.isValidDouble("1A2B", ",", "."));
  }

  @Test
  public void testLongToInt() {
    assertEquals(0, NumberUtility.longToInt(null));
    assertEquals(0, NumberUtility.longToInt(0L));
    assertEquals(42, NumberUtility.longToInt(42L));
    assertEquals(Integer.MIN_VALUE, NumberUtility.longToInt((long) Integer.MIN_VALUE));
    assertEquals(Integer.MAX_VALUE, NumberUtility.longToInt((long) Integer.MAX_VALUE));
    assertEquals(Integer.MIN_VALUE + 1, NumberUtility.longToInt((long) Integer.MIN_VALUE + 1L));
    assertEquals(Integer.MAX_VALUE - 1, NumberUtility.longToInt((long) Integer.MAX_VALUE - 1L));
    assertEquals(Integer.MIN_VALUE, NumberUtility.longToInt((long) Integer.MIN_VALUE - 1L));
    assertEquals(Integer.MAX_VALUE, NumberUtility.longToInt((long) Integer.MAX_VALUE + 1L));
    assertEquals(Integer.MIN_VALUE, NumberUtility.longToInt(Long.MIN_VALUE));
    assertEquals(Integer.MAX_VALUE, NumberUtility.longToInt(Long.MAX_VALUE));
  }

  @Test
  public void testNvl() {
    Number n1 = 1;
    Integer i2 = 2;
    Float f3 = 3.0f;
    assertEquals(1, ObjectUtility.nvl(n1, null));
    assertEquals(Integer.valueOf(2), ObjectUtility.nvl(i2, null));
    assertEquals(Float.valueOf(3f), ObjectUtility.nvl(f3, null));
    assertEquals(1, ObjectUtility.nvl((Number) null, n1));
  }

  @Test
  public void testNvlInteger() {
    Integer value = null;
    assertEquals(100, NumberUtility.nvl(value, 100));
    assertEquals(Integer.valueOf(100), ObjectUtility.nvl(value, Integer.valueOf(100)));
    value = 100;
    assertEquals(100, NumberUtility.nvl(value, 101));
  }

  @Test
  public void testNvlLong() {
    Long value = null;
    assertEquals(100l, NumberUtility.nvl(value, 100));
    assertEquals(Long.valueOf(100l), ObjectUtility.nvl(value, Long.valueOf(100)));
    value = 100l;
    assertEquals(100l, NumberUtility.nvl(value, 101));
  }

  @Test
  public void testNvlFloat() {
    Float value = null;
    assertEquals(100.0f, NumberUtility.nvl(value, 100.0f), 0);
    assertEquals(Float.valueOf(100.0f), ObjectUtility.nvl(value, Float.valueOf(100.0f)));
    value = 100.0f;
    assertEquals(100.0f, NumberUtility.nvl(value, 101.0f), 0);
  }

  @Test
  public void testNvlDouble() {
    Double value = null;
    assertEquals(100.0, NumberUtility.nvl(value, 100.0), 0);
    assertEquals(Double.valueOf(100.0), ObjectUtility.nvl(value, Double.valueOf(100.0)));
    value = 100.0;
    assertEquals(100.0, NumberUtility.nvl(value, 101.0), 0);
  }
}
