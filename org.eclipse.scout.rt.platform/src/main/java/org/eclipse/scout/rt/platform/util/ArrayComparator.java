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

import java.util.Comparator;
import java.util.Locale;

@SuppressWarnings("squid:S2063")
public class ArrayComparator implements Comparator<Object[]> {
  private final ColumnComparator[] m_comparators;

  public ArrayComparator(ColumnComparator... comparators) {
    m_comparators = comparators;
  }

  /**
   * Convenience constructor that creates {@link ColumnComparator ColumnComparators} with {@link DefaultObjectComparator
   * DefaultObjectComparators} for all sort columns.
   *
   * @param locale
   * @param sortCols
   *          0-based column indices
   */
  public ArrayComparator(Locale locale, int... sortCols) {
    m_comparators = new ColumnComparator[sortCols.length];
    for (int i = 0; i < sortCols.length; ++i) {
      m_comparators[i] = new ColumnComparator(sortCols[i], new DefaultObjectComparator(locale));
    }
  }

  @Override
  public int compare(Object[] a, Object[] b) {
    if (m_comparators == null || m_comparators.length == 0) {
      return 0;
    }
    if (a == null && b == null) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }
    for (int i = 0; i < m_comparators.length && m_comparators[i].getColumnIndex() < a.length && m_comparators[i].getColumnIndex() < b.length; i++) {
      Object o1 = a[m_comparators[i].getColumnIndex()];
      Object o2 = b[m_comparators[i].getColumnIndex()];
      int c = m_comparators[i].getComparator().compare(o1, o2);
      if (c != 0) {
        return c;
      }
    }
    return Integer.compare(a.length, b.length);
  }

  /**
   * {@link ColumnComparator} is used for sorting 2d Object arrays. It consists of the two properties
   * {@link #getColumnIndex()} and {@link #getComparator()}.
   */
  public static class ColumnComparator {
    private final int m_columnIndex;
    private final Comparator<Object> m_comparator;

    public ColumnComparator(int columnIndex, Comparator<Object> comparator) {
      m_columnIndex = columnIndex;
      m_comparator = comparator;
    }

    /**
     * @return 0-based index of the column
     */
    public int getColumnIndex() {
      return m_columnIndex;
    }

    /**
     * @return object comparator
     */
    public Comparator<Object> getComparator() {
      return m_comparator;
    }
  }

  /**
   * comparator for objects which follows these rules
   * <ol>
   * <li>Strings are comnpared by {@link StringUtility#compareIgnoreCase(Locale, String, String)}
   * <li>{@link Comparable Comparables} are compared using their natural order.
   * <li>For all other objects the String representation is compared by
   * {@link StringUtility#compareIgnoreCase(Locale, String, String)}.
   * </ol>
   */
  @SuppressWarnings("squid:S2063")
  public static class DefaultObjectComparator implements Comparator<Object> {

    private final Locale m_locale;

    public DefaultObjectComparator(Locale locale) {
      m_locale = locale;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object o1, Object o2) {
      int c = 0;
      if (o1 == null && o2 == null) {
        c = 0;
      }
      else if (o1 == null) {
        c = -1;
      }
      else if (o2 == null) {
        c = 1;
      }
      else if ((o1 instanceof String) && (o2 instanceof String)) {
        c = StringUtility.compareIgnoreCase(m_locale, (String) o1, (String) o2);
      }
      else if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
        c = ((Comparable) o1).compareTo(o2);
      }
      else {
        c = StringUtility.compareIgnoreCase(m_locale, o1.toString(), o2.toString());
      }
      return c;
    }

  }
}
