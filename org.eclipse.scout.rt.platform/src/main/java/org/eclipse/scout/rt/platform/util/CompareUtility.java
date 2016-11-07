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

import java.util.Collection;

/**
 * Utility class for comparing objects considering <code>null</code> values and other commonly used special cases.<br>
 * TODO [7.1] pbz: remove this utility
 */
public final class CompareUtility {

  private CompareUtility() {
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
   * @deprecated Use {@link ObjectUtility#equals(Object, Object)} instead.
   */
  @SuppressWarnings("squid:S1201")
  @Deprecated
  public static <T> boolean equals(T a, T b) {
    return ObjectUtility.equals(a, b);
  }

  /**
   * Indicates whether some Object a is not "equal to" another Object b. See {@link #equals} for more detail.
   *
   * @param a
   *          the second Object to be compared.
   * @param b
   *          the second Object to be compared.
   * @return <code>true</code> if a is not same as b, <code>false</code> otherwise.
   * @deprecated Use {@link ObjectUtility#notEquals(Object, Object)} instead.
   */
  @Deprecated
  public static <T> boolean notEquals(T a, T b) {
    return ObjectUtility.notEquals(a, b);
  }

  /**
   * @deprecated Use {@link ObjectUtility#compareTo(Comparable, Comparable)} instead
   */
  @Deprecated
  public static <T extends Comparable<T>> int compareTo(T a, T b) {
    return ObjectUtility.compareTo(a, b);
  }

  /**
   * @return Returns <code>true</code> if the given object is in the list of the given elements. The objects are
   *         compared using {@link #equals}.
   * @since 3.8.1
   * @deprecated Use {@link ObjectUtility#isOneOf(Object, Object...)} instead.
   */
  @Deprecated
  public static boolean isOneOf(Object o, Object... elements) {
    return ObjectUtility.isOneOf(o, elements);
  }

  /**
   * @return Returns <code>true</code> if the given object is in the list of the given elements. The objects are
   *         compared using {@link #equals}.
   * @since 3.10
   * @deprecated Use {@link ObjectUtility#isOneOf(Object, Collection)} instead.
   */
  @Deprecated
  public static boolean isOneOf(Object o, Collection<?> elements) {
    return ObjectUtility.isOneOf(o, elements);
  }
}
