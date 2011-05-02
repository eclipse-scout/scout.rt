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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ListUtility {

  private ListUtility() {
  }

  public static List<Object> toList(Object... array) {
    ArrayList<Object> list = new ArrayList<Object>();
    if (array != null) {
      for (Object o : array) {
        list.add(o);
      }
    }
    return list;
  }

  public static Object[] toArray(Object... array) {
    return array;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] copyArray(T[] a) {
    T[] copy = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length);
    System.arraycopy(a, 0, copy, 0, a.length);
    return copy;
  }

  public static <T> boolean containsAny(Collection<T> list, Collection<T> values) {
    if (list != null) {
      for (T o : values) {
        if (list.contains(o)) {
          return true;
        }
      }
    }
    return false;
  }

  public static <T> boolean containsAny(Collection<T> list, T... values) {
    if (list != null) {
      for (T o : values) {
        if (list.contains(o)) {
          return true;
        }
      }
    }
    return false;
  }

  public static <T> String format(Collection<T> list, String delimiter) {
    if (list == null) return "";
    StringBuffer buf = new StringBuffer();
    int index = 0;
    for (T o : list) {
      if (index > 0) buf.append(delimiter);
      buf.append("" + o);
      index++;
    }
    return buf.toString();
  }

  public static String format(Collection<?> list) {
    return format(list, false);
  }

  public static <T> String format(Collection<T> c, boolean quoteStrings) {
    StringBuffer buf = new StringBuffer();
    if (c != null) {
      int index = 0;
      for (T o : c) {
        if (index > 0) buf.append(", ");
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

  public static List<Object> parse(String text) {
    List<Object> list = null;
    if (text != null && text.trim().length() > 0) {
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
        else if (s.indexOf(".") >= 0) {
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
    return CollectionUtility.copyList(list);
  }

  /**
   * combine all lists into one list containing all elements the order of the
   * items is preserved
   */
  public static <T> List<T> combine(Collection<T>... collections) {
    List<T> list = null;
    if (collections != null && collections.length > 0) {
      for (Collection<T> c : collections) {
        for (T t : c) {
          list = CollectionUtility.appendList(list, t);
        }
      }
    }
    return CollectionUtility.copyList(list);
  }

  /**
   * @return true if the collection contains at least two equal values
   */
  public static <T> boolean isAmbiguous(Collection<T> c) {
    return !isDistinct(c);
  }

  /**
   * @return true if all values in the collection are distinct
   */
  public static <T> boolean isDistinct(Collection<T> c) {
    HashSet<T> set = new HashSet<T>(c);
    return (set.size() == c.size());
  }

  /**
   * returns the (single) number, if all values are the same in the array,
   * otherwise null
   */
  public static <T> T getUnique(T... n) {
    if (n == null || n.length == 0) return null;
    T retVal = null;
    for (int i = 0; i < n.length; i++) {
      if (n[i] != null) {
        if (retVal == null) {
          retVal = n[i];
        }
        else {
          if (!retVal.equals(n[i])) {
            return null;
          }
        }
      }
    }
    return retVal;
  }

  /**
   * @return the length of an array using {@link Array#getLength(Object)}.
   *         <p>
   *         Accepts arrays, {@link Collection}s, {@link Map}s, null
   *         <p>
   *         if the array has multiple dimensions, returns the first dimension
   *         <p>
   *         if the array is null, returns -1
   *         <p>
   * @throws {@link IllegalArgumentException} if the argument is neither an array nor null
   */
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
}
