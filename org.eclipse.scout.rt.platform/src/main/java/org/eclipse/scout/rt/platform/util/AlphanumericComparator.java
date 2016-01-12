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

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of {@link Comparator} that splits strings up in numeric and non-numeric parts and compares each of
 * them individually with the appropriate method.
 * <p>
 * Example:
 *
 * <pre>
 * Unsorted       Default string sort    Alphanumeric sort
 * --------------------------------------------------------
 * myfile.doc     doc10.txt              doc8
 * doc10.txt      doc8                   doc9.txt
 * doc9.txt       doc9.txt               doc10.txt
 * doc8           myfile.doc             myfile.txt
 * </pre>
 *
 * @since 5.2
 */
public class AlphanumericComparator implements Comparator<String>, Serializable {

  private static final long serialVersionUID = 1L;
  private final boolean m_ignoreCase;

  /**
   * Creates a new alphanumeric comparator with <code>ignoreCase = false</code>.
   */
  public AlphanumericComparator() {
    this(false);
  }

  /**
   * Creates a new alphanumeric comparator.
   *
   * @param ignoreCase
   *          whether the case should be ignored when comparing strings
   */
  public AlphanumericComparator(boolean ignoreCase) {
    m_ignoreCase = ignoreCase;
  }

  public boolean isIgnoreCase() {
    return m_ignoreCase;
  }

  @Override
  public int compare(String s1, String s2) {
    if (s1 == null && s2 == null) {
      return 0;
    }
    if (s1 == null) {
      return -1;
    }
    if (s2 == null) {
      return 1;
    }
    Pattern p = Pattern.compile("(([0-9]+)|([^0-9]+))", Pattern.DOTALL);
    Matcher m1 = p.matcher(s1);
    Matcher m2 = p.matcher(s2);
    boolean m1Found = m1.find();
    boolean m2Found = m2.find();
    while (m1Found && m2Found) {
      String x1 = m1.group(1);
      String x2 = m2.group(1);
      int result = 0;
      try {
        Long n1 = Long.parseLong(x1);
        Long n2 = Long.parseLong(x2);
        result = n1.compareTo(n2);
      }
      catch (NumberFormatException e) {
        // At least one of the strings contains non-numeric characters --> use String comparison
        result = (isIgnoreCase() ? x1.compareToIgnoreCase(x2) : x1.compareTo(x2));
      }
      if (result != 0) {
        return result;
      }
      m1Found = m1.find();
      m2Found = m2.find();
    }
    if (m1.hitEnd() && !m2.hitEnd()) {
      // s2 has more parts
      return -1;
    }
    else if (!m1.hitEnd() && m2.hitEnd()) {
      // s1 has more parts
      return 1;
    }
    // Both are the same
    return 0;
  }
}
