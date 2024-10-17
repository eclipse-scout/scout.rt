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

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.util.date.DateUtility;

/**
 * Utility for null-safe operations on {@link Object}.
 */
public final class ObjectUtility {

  private ObjectUtility() {
  }

  /**
   * @return {@code value} if it is not {@code null}, otherwise {@code valueWhenNull}.
   */
  public static <T> T nvl(T value, T valueWhenNull) {
    if (value != null) {
      return value;
    }
    return valueWhenNull;
  }

  /**
   * @return {@code value} if it is not {@code null}, otherwise the {@code valueSupplierWhenNull} is called to supply an
   *         alternative which is returned. If {@code valueSupplierWhenNull} is absent, {@code null} is returned.
   */
  public static <T> T nvlOpt(T value, Supplier<T> valueSupplierWhenNull) {
    if (value != null) {
      return value;
    }
    return valueSupplierWhenNull != null ? valueSupplierWhenNull.get() : null;
  }

  /**
   * <p>
   * Indicates whether some Object a is "equal to" another Object b. The equivalence relation is implemented according
   * to {@link Object#equals(Object)}.
   * </p>
   * Additionally, some commonly used comparisons are implemented as "expected".
   * <p>
   * <b>Null Values:</b> Any of the arguments may be null. In this case the comparison is implemented as expected:
   * <ul>
   * <li>The method returns <code>true</code>, if both arguments are <code>null</code>.
   * <li>The method returns <code>false</code>, if one argument is <code>null</code> and the other is not
   * <code>null</code>.
   * </ul>
   * </p>
   * <p>
   * <b>Arrays:</b> Arrays are considered "equal to" one another, if all their elements are "equal to" one another.
   * </p>
   * <p>
   * <b>Timestamp and Date:</b> {@link Timestamp#equals(Object)} is not symmetric. E.g. when comparing a Date d and
   * Timestamp t, d.equals(t) may return true while t.equals(d) returns false. This is not "expected" and inconvenient
   * when performing operations like sorting on collections containing both Dates and Timestamps. Therefore, this method
   * defers to {@link DateUtility#equals(Object)} to provide a symmetric implementation of the equivalence comparison.
   * </p>
   *
   * @param a
   *          the first Object to be compared.
   * @param b
   *          the second Object to be compared.
   * @return <code>true</code> if a is the same as b, <code>false</code> otherwise.
   */
  @SuppressWarnings("squid:S1201")
  public static <T> boolean equals(T a, T b) {
    // object level check
    if (a == null && b == null) {
      return true;
    }
    else if (a == null) {
      return false;
    }
    else if (b == null) {
      return false;
    }
    // Special case: defer to DateUtility because of non-symmetry of Timestamp.equals()
    if (a instanceof Date && b instanceof Date) {
      return DateUtility.equals((Date) a, (Date) b);
    }
    if (a.equals(b)) {
      return true;
    }
    // array check
    if (a.getClass().isArray() && b.getClass().isArray()) {
      int na = Array.getLength(a);
      int nb = Array.getLength(b);
      if (na != nb) {
        return false;
      }
      for (int i = 0; i < na; i++) {
        if (!equals(Array.get(a, i), Array.get(b, i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Indicates whether some Object a is not "equal to" another Object b. See {@link #equals} for more detail.
   *
   * @param a
   *          the second Object to be compared.
   * @param b
   *          the second Object to be compared.
   * @return <code>true</code> if a is not same as b, <code>false</code> otherwise.
   */
  public static <T> boolean notEquals(T a, T b) {
    return !equals(a, b);
  }

  /**
   * Null-safe implementation of {@code a.compareTo(b)}.
   *
   * @return
   *         <li>{@code 0} if both values are {@code null}
   *         <li>{@code -1} if a is {@code null} and b is not {@code null}
   *         <li>{@code 1} if b is {@code null} and a is not {@code null}
   *         <li>{@code a.compareTo(b)} if both values are not {@code null}
   */
  public static <T extends Comparable<T>> int compareTo(T a, T b) {
    if (a == null && b == null) {
      return 0;
    }
    else if (a == null) {
      return -1;
    }
    else if (b == null) {
      return 1;
    }
    // Special case: defer to DateUtility because of non-symmetry of Timestamp.compareTo()
    else if (a instanceof Date && b instanceof Date) {
      return DateUtility.compareTo((Date) a, (Date) b);
    }
    else {
      return a.compareTo(b);
    }
  }

  /**
   * Null-safe {@code value.toString()} implementation.
   * <ul>
   * <li>Use {@link Objects#toString(Object, String)} if a default value in case of {@code null} is required<br>
   * <li>Use {@link Objects#toString(Object)} if a a null value should be returned as string "null"
   * </ul>
   *
   * @return {@code value.toString()} if {@code value} is not {@code null}, else {@code null}.
   */
  public static String toString(Object value) {
    return Objects.toString(value, null);
  }

  /**
   * @return {@code true} if the given object is equal to an element in the given array. The objects are compared using
   *         {@link #equals}.
   */
  public static boolean isOneOf(Object o, Object[] elements) {
    if (elements == null || elements.length < 1) {
      return false;
    }
    return isOneOf(o, Arrays.asList(elements));
  }

  /**
   * @return {@code true} if the given object is equal to the first element or an element in the given varargs array.
   *         The objects are compared using {@link #equals}.
   */
  public static boolean isOneOf(Object o, Object first, Object... elements) {
    if (equals(o, first)) {
      return true;
    }
    return isOneOf(o, elements);
  }

  /**
   * @return {@code true} if the given object is equal to an element in the given {@link Collection}. The objects are
   *         compared using {@link #equals}.
   */
  public static boolean isOneOf(Object o, Collection<?> elements) {
    if (CollectionUtility.isEmpty(elements)) {
      return false;
    }
    for (Object e : elements) {
      if (equals(o, e)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return {@code true} if the given object represents an actual value as opposed to an empty value such as null,
   *         empty optional, empty collection, empty string etc.
   */
  public static boolean hasValue(Object o) {
    if (o == null) {
      return false;
    }
    if (o instanceof Optional && ((Optional<?>) o).isEmpty()) {
      return false;
    }
    if (o instanceof String && !StringUtility.hasText((String) o)) {
      return false;
    }
    if (o instanceof Collection && ((Collection<?>) o).isEmpty()) {
      return false;
    }
    if (o instanceof Map && ((Map<?, ?>) o).isEmpty()) {
      return false;
    }
    if (o.getClass().isArray() && Array.getLength(o) == 0) {
      return false;
    }
    return true;
  }
}
