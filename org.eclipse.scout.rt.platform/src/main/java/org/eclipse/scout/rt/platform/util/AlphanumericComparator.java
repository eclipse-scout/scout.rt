/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
   * Creates a new alphanumeric comparator with {@code ignoreCase = false}.
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
    Matcher matcher1 = p.matcher(s1);
    Matcher matcher2 = p.matcher(s2);
    boolean found1 = matcher1.find();
    boolean found2 = matcher2.find();
    while (found1 && found2) {
      String nextGroup1 = matcher1.group(1);
      String nextGroup2 = matcher2.group(1);
      int result = compareAsLongs(nextGroup1, nextGroup2);
      if (result != 0) {
        return result;
      }
      found1 = matcher1.find();
      found2 = matcher2.find();
    }
    return compareFound(found1, found2);
  }

  /**
   * Compares the two strings {@code s1} and {@code s2} as {@link Long}s if possible. If not, a string comparison is
   * done.
   */
  protected int compareAsLongs(String s1, String s2) {
    int result = 0;
    try {
      Long n1 = Long.parseLong(s1);
      Long n2 = Long.parseLong(s2);
      result = n1.compareTo(n2);
    }
    catch (NumberFormatException e) {
      // At least one of the strings contains non-numeric characters --> use String comparison
      result = compareAsStrings(s1, s2);
    }
    return result;
  }

  /**
   * Compares the two strings {@code s1} and {@code s2}.
   */
  protected int compareAsStrings(String s1, String s2) {
    return (isIgnoreCase() ? StringUtility.compareIgnoreCase(s1, s2) : StringUtility.compare(s1, s2));
  }

  /**
   * Compares the {@link Matcher}s {@code m1} and {@code m2} by checking if ends have been hit.
   */
  protected int compareFound(boolean f1, boolean f2) {
    if (!f1 && f2) {
      // s1 has more parts
      return -1;
    }
    else if (f1 && !f2) {
      // s2 has more parts
      return 1;
    }
    // Both are the same
    return 0;
  }
}
