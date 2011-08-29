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

import java.util.Comparator;
import java.util.Locale;

public class ArrayComparator implements Comparator<Object[]> {
  private Locale m_locale;
  private int[] m_sortColumns;

  public ArrayComparator(Locale locale, int... sortCols) {
    m_locale = locale;
    m_sortColumns = sortCols;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compare(Object[] a, Object[] b) {
    if (m_sortColumns == null || m_sortColumns.length == 0) {
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
    for (int i = 0; i < m_sortColumns.length && m_sortColumns[i] < a.length && m_sortColumns[i] < b.length; i++) {
      Object o1 = a[m_sortColumns[i]];
      Object o2 = b[m_sortColumns[i]];
      int c = 0;
      if (o1 == null && o2 == null) {
        c = 0;
      }
      else if (o1 == null) {
        c = -1;
      }
      else if (o2 == null && o2 == null) {
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
      if (c != 0) {
        return c;
      }
    }
    if (a.length < b.length) {
      return -1;
    }
    if (a.length > b.length) {
      return 1;
    }
    return 0;
  }
}
