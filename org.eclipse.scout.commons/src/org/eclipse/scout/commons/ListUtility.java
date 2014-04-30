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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Will be removed in Scout 5.0. Use {@link CollectionUtility} instead.
 */
@Deprecated
public final class ListUtility {

  private ListUtility() {
  }

  /**
   * @deprecated Will be removed in Scout 5.0. Use {@link CollectionUtility#arrayList(Object...)} instead.
   */
  @Deprecated
  public static List<Object> toList(Object... array) {
    ArrayList<Object> list = new ArrayList<Object>();
    if (array != null) {
      for (Object o : array) {
        list.add(o);
      }
    }
    return list;
  }

  /**
   * @deprecated Will be removed in Scout 5.0.
   */
  @Deprecated
  public static <T> T[] toArray(T... array) {
    return array;
  }

  /**
   * @deprecated Will be removed in Scout 5.0. Use {@link Arrays#copyOf(Object[], int)} instead.
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static <T> T[] copyArray(T[] a) {
    T[] copy = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length);
    System.arraycopy(a, 0, copy, 0, a.length);
    return copy;
  }

  /**
   * @return true if list contains any (at least 1) of the values.
   * @deprecated Will be removed in Scout 5.0. Use {@link CollectionUtility#containsAny(Collection, Collection)}
   *             instead.
   */
  @Deprecated
  public static <T> boolean containsAny(Collection<T> list, Collection<T> values) {
    return CollectionUtility.containsAny(list, values);
  }

  /**
   * @return true if list contains any (at least 1) of the values
   * @deprecated Will be removed in Scout 5.0. Use {@link CollectionUtility#containsAny(Collection, Object...)} instead.
   */
  @Deprecated
  public static <T> boolean containsAny(Collection<T> list, T... values) {
    return CollectionUtility.containsAny(list, values);
  }

  /**
   * @deprecated use {@link CollectionUtility#format(Collection, String)} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public static <T> String format(Collection<T> list, String delimiter) {

    if (list == null) {
      return "";
    }
    StringBuilder buf = new StringBuilder();
    int index = 0;
    for (T o : list) {
      if (index > 0) {
        buf.append(delimiter);
      }
      buf.append("" + o);
      index++;
    }
    return buf.toString();
  }

  /**
   * @deprecated use {@link CollectionUtility#format(Collection)} instead. Will be removed in the 5.0 Release
   */
  @Deprecated
  public static String format(Collection<?> list) {
    return CollectionUtility.format(list);
  }

  /**
   * @deprecated use {@link CollectionUtility#format(Collection, boolean)} instead. Will be removed in the 5.0 Release
   */
  @Deprecated
  public static <T> String format(Collection<T> c, boolean quoteStrings) {
    StringBuffer buf = new StringBuffer();
    if (c != null) {
      int index = 0;
      for (T o : c) {
        if (index > 0) {
          buf.append(", ");
        }
        String s;
        if (o instanceof Number) {
          s = o.toString();
        }
        else {
          if (quoteStrings) {
            s = "'" + ("" + o).replaceAll(",", "%2C") + "'";
          }
          else {
            s = "" + o;
          }
        }
        buf.append(s);
        index++;
      }
    }
    return buf.toString();
  }

  /**
   * @deprecated use {@link CollectionUtility#parse(String)} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public static List<Object> parse(String text) {
    List<Object> list = null;
    if (StringUtility.hasText(text)) {
      String[] a = text.split(",");
      for (String s : a) {
        Object o;
        // remove escaped ','
        s = s.replaceAll("%2C", ",");
        if (s.equalsIgnoreCase("null")) {
          o = null;
        }
        else if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
          o = s.substring(1, s.length() - 2);
        }
        else if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
          o = s.substring(1, s.length() - 2);
        }
        else if (s.indexOf('.') >= 0) {
          // try to make double
          try {
            o = new Double(Double.parseDouble(s));
          }
          catch (Exception e) {
            /* nop */
            o = s;
          }
        }
        else {
          // try to make long
          try {
            o = new Long(Long.parseLong(s));
          }
          catch (Exception e) {
            /* nop */
            o = s;
          }
        }
        list = CollectionUtility.appendList(list, o);
      }
    }
    return CollectionUtility.arrayList(list);
  }

  /**
   * @deprecated Will be removed in Scout 5.0. Use {@link CollectionUtility#combine(Collection...)} instead.
   */
  @Deprecated
  public static <T> List<T> combine(Collection<T>... collections) {
    return CollectionUtility.combine(collections);
  }

  /**
   * @deprecated use {@link CollectionUtility#hasElements(Object[])} instead. Will be removed in the 5.0 Release
   */
  @Deprecated
  public static int length(Object array) {
    if (array == null) {
      return -1;
    }
    if (array.getClass().isArray()) {
      return Array.getLength(array);
    }
    if (array instanceof Collection<?>) {
      return ((Collection<?>) array).size();
    }
    if (array instanceof Map<?, ?>) {
      return ((Map<?, ?>) array).size();
    }
    throw new IllegalArgumentException("expected one of: null, array, collection, map");
  }

  /**
   * @deprecated use {@link CollectionUtility#arrayList(Object...)} instead. Will be removed in the 5.0 Release
   */
  @Deprecated
  public static <T> ArrayList<T> arrayList(T... values) {
    return CollectionUtility.arrayList(values);
  }

  /**
   * @deprecated use {@link CollectionUtility#arrayList(Object)} instead. Will be removed in the 5.0 Release
   */
  @Deprecated
  public static <T> ArrayList<T> arrayList(T value) {
    return CollectionUtility.arrayList(value);
  }

  /**
   * @deprecated use {@link CollectionUtility#getElement(List, int)} instead. Will be removed in the 5.0 Release
   */
  @Deprecated
  public static <T> T getElement(List<? extends T> list, int index) {
    return CollectionUtility.getElement(list, index);
  }
}
