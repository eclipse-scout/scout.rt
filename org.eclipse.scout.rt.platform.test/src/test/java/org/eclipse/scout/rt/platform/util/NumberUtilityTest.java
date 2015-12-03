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
import static org.junit.Assert.assertNull;

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
  public void testParseEmptyString() {
    assertEquals(0d, NumberUtility.parseDouble(""), 0.00001d);
    assertEquals(0l, NumberUtility.parseLong(""));
    assertEquals(0, NumberUtility.parseInt(""));

  }
}
