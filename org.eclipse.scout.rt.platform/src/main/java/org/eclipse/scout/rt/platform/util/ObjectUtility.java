package org.eclipse.scout.rt.platform.util;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;

/**
 * Utility for null-safe operations on {@link Object}.
 */
public final class ObjectUtility {

  private ObjectUtility() {
  }

  /**
   * @return {@code value} if it is not {@code null} otherwise {@code valueWhenNull}.
   */
  public static <T> T nvl(T value, T valueWhenNull) {
    if (value != null) {
      return value;
    }
    return valueWhenNull;
  }

  /**
   * <p>
   * Indicates whether some Object a is "equal to" another Object b. The equivalence relation is implemented according
   * to {@link java.lang.Object#equals(Object)}.
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
   * <b>Timestamp and Date:</b> {@link java.sql.Timestamp#equals(Object)} is not symmetric. E.g. when comparing a Date d
   * and Timestamp t, d.equals(t) may return true while t.equals(d) returns false. This is not "expected" and
   * inconvenient when performing operations like sorting on collections containing both Dates and Timestamps.
   * Therefore, this method handles <code> java.sql.Timestamp</code> specifically to provide a symmetric implementation
   * of the equivalence comparison.
   * </p>
   * <p>
   * <code>java.sql.Timestamp</code> is a subclass of <code>java.util.Date</code>, which additionally allows to specify
   * fractional seconds to a precision of nanoseconds. <br>
   * This method returns <code>true</code>, if and only if both arguments of Type <code>java.util.Date</code> or
   * <code>java.sql.Timestamp</code> represent the same point in time.
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
    // Special case: If 'a' is Timestamp and 'b' is not, flip comparison order (because of non-symmetry of Timestamp.equals())
    if (a instanceof Timestamp && !(b instanceof Timestamp)) {
      return b.equals(a);
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
   * Null-safe implementation of {@code a.compareTo(b)}
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
    else {
      return a.compareTo(b);
    }
  }

  /**
   * @return Returns <code>true</code> if the given object is in the list of the given elements. The objects are
   *         compared using {@link #equals}.
   */
  public static boolean isOneOf(Object o, Object... elements) {
    if (elements == null) {
      return false;
    }
    return isOneOf(o, Arrays.asList(elements));
  }

  /**
   * @return Returns <code>true</code> if the given object is in the list of the given elements. The objects are
   *         compared using {@link ObjectUtility#equals}.
   */
  public static boolean isOneOf(Object o, Collection<?> elements) {
    if (CollectionUtility.isEmpty(elements)) {
      return false;
    }
    for (Object e : elements) {
      if (ObjectUtility.equals(o, e)) {
        return true;
      }
    }
    return false;
  }
}
