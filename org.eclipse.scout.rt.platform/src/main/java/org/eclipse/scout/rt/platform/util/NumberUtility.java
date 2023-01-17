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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Random;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NumberUtility {
  private static final Logger LOG = LoggerFactory.getLogger(NumberUtility.class);
  @SuppressWarnings("squid:S2245") // pseudo-random number generator for cases where cryptographic strength is not required
  private static final Random INSECURE_RANDOM = new Random();

  private NumberUtility() {
  }

  /**
   * Converts a Number to a Double.
   *
   * @param n
   *          Number to be converted.
   * @return The converted Double. Returns <code>null</code> when the input parameter is <code>null</code>.
   */
  public static Double toDouble(Number n) {
    if (n == null) {
      return null;
    }
    if (n instanceof Float) {
      // rounding error workaround
      return Double.valueOf(n.toString());
    }
    else {
      return n.doubleValue();
    }
  }

  /**
   * Divides dividend by divisor and rounds up to the next integer value.
   * <p>
   * More formally: Performs the same calculation as {@code (int) Math.ceil((double) dividend / divisor)} but without
   * using floating point arithmetics.
   *
   * @param dividend
   *          The dividend
   * @param divisor
   *          The divisor. Must not be zero!
   * @return {@code (int)ceil(dividend / divisor)}
   * @throws ArithmeticException
   *           if divisor is zero.
   */
  public static int divideAndCeil(int dividend, int divisor) {
    return (dividend + divisor - 1) / divisor;
  }

  /**
   * Converts a Number to an Integer.
   *
   * @param n
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
   * @param n
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
   */
  public static BigDecimal numberToBigDecimal(Number number) {
    if (number instanceof BigDecimal) {
      return (BigDecimal) number;
    }
    if (number == null) {
      return null;
    }
    BigDecimal retVal = null;
    try {
      retVal = new BigDecimal(number.toString());
    }
    catch (NumberFormatException e) {
      LOG.warn("converting to BigDecimal failed for Number: '{}'", number);
    }
    return retVal;
  }

  /**
   * Converts a Double to a BigDecimal.
   *
   * @param d
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
   * @param l
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
   * Computes the median of a vararg of doubles.
   *
   * @param a
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
   * @return <code>true</code> if the bit at position index is set, <code>false</code> otherwise.
   */
  public static boolean hasBit(int value, int index) {
    return getBit(value, index);
  }

  /**
   * Checks whether the bit at a given position is set.
   *
   * @return <code>true</code> if the bit at position index is set, <code>false</code> otherwise.
   */
  public static boolean getBit(int value, int index) {
    return (value & (1 << index)) != 0;
  }

  /**
   * Calculates the sum of a Collection of Numbers. Elements that have no valid BigDecimal representation (e.g.
   * Double.NaN, null, ...) are treated as <code>BigDecimal.ZERO</code>
   */
  public static BigDecimal sum(Collection<? extends Number> numbers) {
    BigDecimal sum = BigDecimal.ZERO;
    if (CollectionUtility.hasElements(numbers)) {
      for (Number number : numbers) {
        if (number != null) {
          BigDecimal decimal = numberToBigDecimal(number);
          sum = sum.add(decimal == null ? BigDecimal.ZERO : decimal);
        }
      }
    }
    return sum;
  }

  /**
   * Convenience override that delegates to {@link NumberUtility#sum(Collection)}
   */
  public static BigDecimal sum(Number... a) {
    if (a == null) {
      return BigDecimal.ZERO;
    }
    if (a.length == 0) {
      return BigDecimal.ZERO;
    }
    return sum(Arrays.asList(a));
  }

  /**
   * Computes the minimum value of a vararg of doubles.
   *
   * @param a
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
   * @param a
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
   * @return an insecure random long using <code>{@link Random#nextLong()}</code><br>
   *         For secure random numbers use {@link SecurityUtility#createSecureRandom()}.
   */
  public static long randomLong() {
    return INSECURE_RANDOM.nextLong();
  }

  /**
   * @return an insecure random integer using <code>{@link Random#nextInt()}</code><br>
   *         For secure random numbers use {@link SecurityUtility#createSecureRandom()}.
   */
  public static int randomInt() {
    return INSECURE_RANDOM.nextInt();
  }

  /**
   * @return an insecure random integer using <code>{@link Random#nextInt(int)}</code><br>
   *         For secure random numbers use {@link SecurityUtility#createSecureRandom()}.
   */
  public static int randomInt(int size) {
    return INSECURE_RANDOM.nextInt(size);
  }

  /**
   * @return an insecure random double using <code>{@link Random#nextDouble()}</code><br>
   *         For secure random numbers use {@link SecurityUtility#createSecureRandom()}.
   */
  public static double randomDouble() {
    return INSECURE_RANDOM.nextDouble();
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
   * @return The value with the bit at the given index set to 1.
   */
  public static int setBit(int value, int index) {
    return value | (1 << index);
  }

  /**
   * Clears a bit of a value at a given position.
   *
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
      return 0;
    }
    double d = n.doubleValue();
    if (d < 0) {
      return -1;
    }
    if (d > 0) {
      return +1;
    }
    return 0;
  }

  /**
   * Converts a String to an int
   *
   * @return the converted int. Returns 0 if the parameter is <code>null</code> or empty.
   */
  public static int parseInt(String s) {
    if (StringUtility.isNullOrEmpty(s)) {
      return 0;
    }
    Integer intValue = TypeCastUtility.castValue(s, Integer.class);
    return intValue;
  }

  /**
   * Converts a String to a long
   *
   * @return The converted long. Returns 0 if the parameter is <code>null</code> or empty.
   */
  public static long parseLong(String s) {
    if (StringUtility.isNullOrEmpty(s)) {
      return 0;
    }
    Long longValue = TypeCastUtility.castValue(s, Long.class);
    return longValue;
  }

  /**
   * Converts a String to a double
   *
   * @return The converted double. Returns 0 if the parameter is <code>null</code> or empty.
   */
  public static double parseDouble(String s) {
    if (StringUtility.isNullOrEmpty(s)) {
      return 0;
    }
    Double doubleValue = TypeCastUtility.castValue(s, Double.class);
    return doubleValue;
  }

  /**
   * Formats a number with the <code>NlsLocale</code>
   *
   * @return The formatted number. Returns an empty string if the parameter is <code>null</code>.
   */
  public static String format(Number n) {
    if (n == null) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return BEANS.get(NumberFormatProvider.class).getNumberInstance(loc).format(n);
  }

  /**
   * Compares the given double values. Returns {@code true} if the difference between the two double values is bigger
   * than the given epsilon.<br>
   * <br>
   * Special cases:
   * <ul>
   * <li>{@code -0} and {@code +0} are considered to be equal even though
   * {@code Double.valueOf(0d).equals(Double.valueOf(-0d))} returns {@code false}! This means this method behaves like
   * {@code +0 == -0}.</li>
   * <li>{@link Double#NaN} and {@link Double#NaN} are considered to be equal even though
   * {@code Double.NaN == Double.NaN} returns {@code false}!</li>
   * </ul>
   *
   * @param d1
   *          The first double value
   * @param d2
   *          The second double value
   * @param epsilon
   *          The difference between the two to be considered equal.
   * @return {@code false} if the difference between the two values is less or equal to the given epsilon.
   */
  public static boolean isDoubleDifferent(double d1, double d2, double epsilon) {
    if (Double.compare(d1, d2) == 0) {
      // handles NaN, Double.POSITIVE_INFINITY and Double.NEGATIVE_INFINITY
      return false;
    }
    return !(Math.abs(d1 - d2) <= Math.abs(epsilon));
  }

  /**
   * @return {@code value} if not {@code null} otherwise {@code valueWhenNull}.
   */
  public static int nvl(Integer value, int valueWhenNull) {
    if (value != null) {
      return value.intValue();
    }
    return valueWhenNull;
  }

  /**
   * @return {@code value} if not {@code null} otherwise {@code valueWhenNull}.
   */
  public static long nvl(Long value, long valueWhenNull) {
    if (value != null) {
      return value.longValue();
    }
    return valueWhenNull;
  }

  /**
   * @return {@code value} if not {@code null} otherwise {@code valueWhenNull}.
   */
  public static float nvl(Float value, float valueWhenNull) {
    if (value != null) {
      return value.floatValue();
    }
    return valueWhenNull;
  }

  /**
   * @return {@code value} if not {@code null} otherwise {@code valueWhenNull}.
   */
  public static double nvl(Double value, double valueWhenNull) {
    if (value != null) {
      return value.doubleValue();
    }
    return valueWhenNull;
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
   * @return The converted int. If the Long is larger than Integer.MAX_VALUE, it returns Integer.MAX_VALUE. If the Long
   *         is smaller than Integer.MIN_VALUE, it returns Integer.MIN_VALUE. If the parameter is <code>null</code>, it
   *         returns 0.
   */
  public static int longToInt(Long l) {
    if (l != null) {
      if (l.longValue() > Integer.MAX_VALUE) {
        return Integer.MAX_VALUE;
      }
      else if (l.longValue() < Integer.MIN_VALUE) {
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
   *         representation of the object is a zero length string.
   */
  public static BigDecimal getBigDecimalValue(Object o) {
    if (o != null && !o.toString().isEmpty()) {
      return new BigDecimal(o.toString());
    }
    else {
      return null;
    }
  }

  /**
   * Returns <code>true</code> if, and only if the given String is a valid number according to the given separators
   *
   * @param str
   *          {@link String}
   * @param decimalSeparator
   *          {@link String}
   * @param thousandsSeparator
   *          {@link String}
   * @return {@code true} if, and only if the given String is a valid number according to the given separators
   */
  public static boolean isValidDouble(String str, String decimalSeparator, String thousandsSeparator) {
    if (str == null || str.isEmpty()) {
      return true;
    }
    if (thousandsSeparator != null && !thousandsSeparator.isEmpty()) {
      str = str.replace(thousandsSeparator, "");
    }
    String regex = "[+-]?\\d*";
    if (decimalSeparator != null && !decimalSeparator.isEmpty()) {
      regex += "(\\" + decimalSeparator + "\\d+)?";
    }
    return str.matches(regex);
  }
}
