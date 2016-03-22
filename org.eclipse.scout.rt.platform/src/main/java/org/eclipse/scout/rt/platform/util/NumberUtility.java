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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Random;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NumberUtility {
  private static final Logger LOG = LoggerFactory.getLogger(NumberUtility.class);
  private static final Random UNSECURE_RANDOM = new Random();

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
   * Calculates the sum of a Collection of Numbers. Elements that have no valid BigDecimal representation (e.g.
   * Double.NaN, null, ...) are treated as <code>BigDecimal.ZERO</code>
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
   * @return an unsecure random long using <code>{@link Random#nextLong()}</code>
   *         <p>
   *         for secure random numbers see SecureRandom
   */
  public static long randomLong() {
    return UNSECURE_RANDOM.nextLong();
  }

  /**
   * @return an unsecure random int using <code>{@link Random#nextInt()</code>
   *         <p>
   *         for secure random numbers see SecureRandom
   */
  public static int randomInt() {
    return UNSECURE_RANDOM.nextInt();
  }

  /**
   * @param size
   * @return an unsecure random int using <code>{@link Random#nextInt(size)</code>
   *         <p>
   *         for secure random numbers see SecureRandom
   */
  public static int randomInt(int size) {
    return UNSECURE_RANDOM.nextInt(size);
  }

  /**
   * @return an unsecure random double using <code>{@link Random#nextDouble()</code>
   *         <p>
   *         for secure random numbers see SecureRandom
   */
  public static double randomDouble() {
    return UNSECURE_RANDOM.nextDouble();
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
      return Integer.valueOf(-1);
    }
    if (d > 0) {
      return Integer.valueOf(+1);
    }
    return Integer.valueOf(0);
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
   * @return The converted int. If the Long is larger than Integer.MAX_VALUE, it returns Integer.MAX_VALUE. If the Long
   *         is smaller than Integer.MIN_VALUE, it returns Integer.MIN_VALUE. If the parameter is <code>null</code>, it
   *         returns 0.
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
   *         representation of the object is a zero length string.
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
   * Returns <code>true</code> if, and only if the given String is a valid number according to the given separators
   *
   * @param str
   *          {@link String}
   * @param decimalSeparator
   *          {@link String}
   * @param thousandsSeparator
   *          {@link String}
   * @returns <code>true</code> if, and only if the given String is a valid number according to the given separators
   */
  public static boolean isValidDouble(String str, String decimalSeparator, String thousandsSeparator) {
    if (str == null || str.length() == 0) {
      return true;
    }
    if (thousandsSeparator != null && thousandsSeparator.length() > 0) {
      str = str.replace(thousandsSeparator, "");
    }
    String regex = "[+-]?\\d*";
    if (decimalSeparator != null && decimalSeparator.length() > 0) {
      regex += "(\\" + decimalSeparator + "\\d+)?";
    }
    return str.matches(regex);
  }
}
