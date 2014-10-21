/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Random;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class NumberUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NumberUtility.class);
  private static final Random RANDOMIZER = new Random();

  private NumberUtility() {
  }

  /**
   * Converts a Number to a Double.
   *
   * @param The
   *          Number to be converted.
   * @return The converted Double. Returns <code>null</code> when the input parameter is <code>null</code>.
   */
  public static Double toDouble(Number n) {
    if (n == null) {
      return null;
    }
    if (n instanceof Float) {
      // rounding error workaround
      return new Double(n.toString());
    }
    else {
      return new Double(n.doubleValue());
    }
  }

  /**
   * Converts a Number to an Integer.
   *
   * @param The
   *          Number to be converted.
   * @return The converted Integer. Returns <code>null</code> when the input parameter is <code>null</code>.
   */
  public static Integer toInteger(Number n) {
    if (n == null) {
      return null;
    }
    return n.intValue();
  }

  /**
   * Converts a Number to a Long.
   *
   * @param The
   *          Number to be converted.
   * @return The converted Long. Returns <code>null</code> when the input parameter is <code>null</code>.
   */
  public static Long toLong(Number n) {
    if (n == null) {
      return null;
    }
    return n.longValue();
  }

  /**
   * Converts a Number into a BigDecimal using its String representation. If the <code>number</code>'s String
   * representation cannot be converted to a BigDecimal this method returns <code>null</code> (e.g. for Double and
   * Floats special values: NaN, POSITIVE_INFINITY and NEGATIVE_INFINITY).
   *
   * @param number
   */
  public static BigDecimal numberToBigDecimal(Number number) {
    if (number == null) {
      return null;
    }
    BigDecimal retVal = null;
    try {
      retVal = new BigDecimal(number.toString());
    }
    catch (NumberFormatException e) {
      LOG.warn("converting to BigDecimal failed for Number: " + number.toString());
    }
    return retVal;
  }

  /**
   * Converts a Double to a BigDecimal.
   *
   * @param The
   *          Double to be converted.
   * @return The converted BigDecimal. Returns <code>null</code> when the input parameter is <code>null</code>.
   */
  public static BigDecimal toBigDecimal(Double d) {
    if (d == null) {
      return null;
    }
    return BigDecimal.valueOf(d);
  }

  /**
   * Converts a Long to a BigInteger.
   *
   * @param The
   *          Long to be converted.
   * @return The converted BigInteger. Returns <code>null</code> when the input parameter is <code>null</code>.
   */
  public static BigInteger toBigInteger(Long l) {
    if (l == null) {
      return null;
    }
    return BigInteger.valueOf(l);
  }

  /**
   * Computes the average value of a vararg of doubles.
   *
   * @param The
   *          vararg of doubles.
   * @return The average value. Returns 0 if the parameter is null or an empty array.
   */
  public static double avg(double... a) {
    if (a == null) {
      return 0;
    }
    long count = a.length;
    double sum = sum(a);
    if (count > 0) {
      return sum / count;
    }
    else {
      return 0;
    }
  }

  /**
   * Computes the median of a vararg of doubles.
   *
   * @param The
   *          vararg of doubles.
   * @return The median. Returns 0 if the parameter is null or an empty array.
   */
  public static double median(double... a) {
    if (a == null) {
      return 0;
    }
    int count = a.length;
    double[] b = new double[a.length];
    System.arraycopy(a, 0, b, 0, count);
    Arrays.sort(b);
    if (count > 0) {
      return b[count / 2];
    }
    else {
      return 0;
    }
  }

  /**
   * Checks whether the bit at a given position is set.
   *
   * @param value
   * @param index
   * @return <code>true</code> if the bit at position index is set, <code>false</code> otherwise.
   */
  public static boolean hasBit(int value, int index) {
    return getBit(value, index);
  }

  /**
   * Checks whether the bit at a given position is set.
   *
   * @param value
   * @param index
   * @return <code>true</code> if the bit at position index is set, <code>false</code> otherwise.
   */
  public static boolean getBit(int value, int index) {
    return (value & (1 << index)) != 0;
  }

  /**
   * Computes the sum of a vararg of doubles.
   *
   * @param The
   *          vararg of doubles.
   * @return The sum. Returns 0 if the parameter is null or the length of the vararg is 0.
   */
  public static double sum(double... a) {
    if (a == null) {
      return 0;
    }
    if (a.length == 0) {
      return 0;
    }
    double sum = 0;
    for (double d : a) {
      sum += d;
    }
    return sum;
  }

  /**
   * Computes the sum of a Collection of Numbers. Is <code>null</code> lenient.
   *
   * @param numbers
   * @return the sum of all numbers passed in the given list. Null elements are not considered.
   */
  public static BigDecimal sum(Collection<? extends Number> numbers) {
    BigDecimal sum = BigDecimal.ZERO;
    if (CollectionUtility.hasElements(numbers)) {
      for (Number number : numbers) {
        if (number != null) {
          BigDecimal augend = numberToBigDecimal(number);
          sum = sum.add(augend == null ? BigDecimal.ZERO : augend);
        }
      }
    }
    return sum;
  }

  /**
   * Calculates the sum over an array of numbers. Array elements that have no valid BigDecimal representation (e.g.
   * Double.NaN) are treated as <code>BigDecimal.ZERO</code>.
   */
  public static BigDecimal sum(Number... a) {
    if (a == null) {
      return BigDecimal.ZERO;
    }
    if (a.length == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (Number d : a) {
      BigDecimal augend = numberToBigDecimal(d);
      sum = sum.add(augend == null ? BigDecimal.ZERO : augend);
    }
    return sum;
  }

  /**
   * Computes the sum of a vararg of longs.
   *
   * @param The
   *          vararg of longs.
   * @return The sum. Returns 0 if the parameter is null or the length of the vararg is 0.
   */
  public static long sum(long... a) {
    if (a == null) {
      return 0;
    }
    if (a.length == 0) {
      return 0;
    }
    long sum = 0;
    for (long d : a) {
      sum += d;
    }
    return sum;
  }

  /**
   * Computes the minimum value of a vararg of doubles.
   *
   * @param The
   *          vararg of doubles.
   * @return The minimum. Returns 0 if the parameter is null or the length of the vararg is 0.
   */
  public static double min(double... a) {
    if (a == null) {
      return 0;
    }
    if (a.length == 0) {
      return 0;
    }
    double min = Double.MAX_VALUE;
    for (double d : a) {
      min = Math.min(min, d);
    }
    return min;
  }

  /**
   * Computes the maximum value of a vararg of doubles.
   *
   * @param The
   *          vararg of doubles.
   * @return The maximum. Returns 0 if the parameter is null or the length of the vararg is 0.
   */
  public static double max(double... a) {
    if (a == null) {
      return 0;
    }
    if (a.length == 0) {
      return 0;
    }
    double max = Double.MIN_VALUE;
    for (double d : a) {
      max = Math.max(max, d);
    }
    return max;
  }

  /**
   * @return a random long using <code>RANDOMIZER.nextLong()</code>
   */
  public static long randomLong() {
    return RANDOMIZER.nextLong();
  }

  /**
   * @return a random int using <code>RANDOMIZER.nextInt()</code>
   */
  public static int randomInt() {
    return RANDOMIZER.nextInt();
  }

  /**
   * @param size
   * @return a random int using <code>RANDOMIZER.nextInt(size)</code>
   */
  public static int randomInt(int size) {
    return RANDOMIZER.nextInt(size);
  }

  /**
   * @return a random double using <code>RANDOMIZER.nextDouble()</code>
   */
  public static double randomDouble() {
    return RANDOMIZER.nextDouble();
  }

  /**
   * rounding with big decimal precision
   */
  public static Number roundBigDecimal(Number value, double precision) {
    if (value != null) {
      BigDecimal val = value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
      BigDecimal prec = BigDecimal.valueOf(precision);
      return val.divide(prec, BigDecimal.ROUND_HALF_EVEN).setScale(0, BigDecimal.ROUND_HALF_EVEN).multiply(prec);
    }
    return null;
  }

  /**
   * rounding with decimal precision
   */
  public static double round(double value, double precision) {
    return Math.round(value / precision) * precision;
  }

  /**
   * Sets a bit of a value at a given position.
   *
   * @param value
   * @param index
   * @return The value with the bit at the given index set to 1.
   */
  public static int setBit(int value, int index) {
    return value | (1 << index);
  }

  /**
   * Clears a bit of a value at a given position.
   *
   * @param value
   * @param index
   * @return The value with the bit at the given index set to 0.
   */
  public static int clearBit(int value, int index) {
    return value & (value ^ (1 << index));
  }

  /**
   * Calculates the signum of a Number
   *
   * @return The signum.
   */
  public static Number sign(Number n) {
    if (n == null) {
      return Integer.valueOf(0);
    }
    double d = n.doubleValue();
    if (d < 0) {
      return new Integer(-1);
    }
    if (d > 0) {
      return new Integer(+1);
    }
    return new Integer(0);
  }

  /**
   * Converts a String to an int
   *
   * @return the converted int. Returns 0 if the parameter is <code>null</code>.
   */
  public static int parseInt(String s) {
    if (s == null) {
      return 0;
    }
    return TypeCastUtility.castValue(s, Integer.class);
  }

  /**
   * Converts a String to a long
   *
   * @return The converted long. Returns 0 if the parameter is <code>null</code>.
   */
  public static long parseLong(String s) {
    if (s == null) {
      return 0;
    }
    return TypeCastUtility.castValue(s, Long.class);
  }

  /**
   * Converts a String to a double
   *
   * @return The converted double. Returns 0 if the parameter is <code>null</code>.
   */
  public static double parseDouble(String s) {
    if (s == null) {
      return 0;
    }
    return TypeCastUtility.castValue(s, Double.class);
  }

  /**
   * Formats a number with the <code>LocaleThreadLocal</code>
   *
   * @return The formatted number. Returns an empty string if the parameter is <code>null</code>.
   */
  public static String format(Number n) {
    if (n == null) {
      return "";
    }
    Locale loc = LocaleThreadLocal.get();
    return NumberFormat.getInstance(loc).format(n);
  }

  /**
   * Imitates the <code>nvl</code> function of Oracle SQL.
   *
   * @param value
   * @param valueWhenNull
   * @return value if value is not <code>null</code>, otherwise valueWhenNull.
   */
  public static <T> T nvl(T value, T valueWhenNull) {
    if (value != null) {
      return value;
    }
    else {
      return valueWhenNull;
    }
  }

  /**
   * Imitates the <code>nvl</code> function of Oracle SQL.
   *
   * @param value
   * @param valueWhenNull
   * @return value if value is not <code>null</code>, otherwise valueWhenNull
   */
  public static int nvl(Integer value, Number valueWhenNull) {
    if (value != null) {
      return value;
    }
    else {
      return valueWhenNull.intValue();
    }
  }

  /**
   * Imitates the <code>nvl</code> function of Oracle SQL.
   *
   * @param value
   * @param valueWhenNull
   * @return value if value is not <code>null</code>, otherwise valueWhenNull
   */
  public static long nvl(Long value, Number valueWhenNull) {
    if (value != null) {
      return value;
    }
    else {
      return valueWhenNull.longValue();
    }
  }

  /**
   * trunc a floating number to integer without rounding it
   */
  public static double trunc(Number n) {
    if (n == null) {
      return 0;
    }
    return Math.floor(n.doubleValue());
  }

  /**
   * Converts a Long to an int with special attention to overflow issues.
   *
   * @return The converted int. If the Long is larger than Integer.MAX_VALUE, it returns Integer.MAX_VALUE.
   *         If the Long is smaller than Integer.MIN_VALUE, it returns Integer.MIN_VALUE.
   *         If the parameter is <code>null</code>, it returns 0.
   */
  public static int longToInt(Long l) {
    if (l != null) {
      if (new Long(Integer.MAX_VALUE).compareTo(l) == -1) {
        return Integer.MAX_VALUE;
      }
      else if (new Long(Integer.MIN_VALUE).compareTo(l) == 1) {
        return Integer.MIN_VALUE;
      }
      return l.intValue();
    }
    return 0;
  }

  /**
   * Converts an object to a BigDecimal using the string representation of the object.
   *
   * @return The converted BigDecimal. Returns <code>null</code> if the object is <code>null</code> or the string
   *         representation of the object is a zero
   *         length string.
   */
  public static BigDecimal getBigDecimalValue(Object o) {
    if (o != null && o.toString().length() > 0) {
      return new BigDecimal(o.toString());
    }
    else {
      return null;
    }
  }

  /**
   * Divides two double values, NPE save.
   *
   * @param numerator
   * @param denominator
   * @return null if the numerator is null or the denominator is null or 0.0.
   *         Else it returns the division result.
   */
  public static Double divide(Double numerator, Double denominator) {
    if (numerator == null || denominator == null || denominator.equals(0.0)) {
      return null;
    }
    else if (numerator.equals(0.0)) {
      return 0d;
    }
    else {
      return numerator / denominator;
    }
  }
}
