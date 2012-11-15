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

import java.lang.reflect.Array;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class CompareUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CompareUtility.class);

  private CompareUtility() {
  }

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

  public static <T> boolean notEquals(T a, T b) {
    return !equals(a, b);
  }

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
   *         compared using {@link #equals(Object)}.
   * @since 3.8.1
   */
  public static boolean isOneOf(Object o, Object... elements) {
    if (elements == null || elements.length == 0) {
      return false;
    }
    for (Object e : elements) {
      if (CompareUtility.equals(o, e)) {
        return true;
      }
    }
    return false;
  }
}
