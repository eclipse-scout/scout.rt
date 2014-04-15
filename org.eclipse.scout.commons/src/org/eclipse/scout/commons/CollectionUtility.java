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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public final class CollectionUtility {
  private CollectionUtility() {
  }

  /**
   * compares the two collection of same content in any order. Is overloaded ({@link #equalsCollection(List, List)} for
   * lists where the order of the list is considered.
   * 
   * @param c1
   * @param c2
   * @return true if the two collections contains the same elements in any order.
   */
  public static <T> boolean equalsCollection(Collection<? extends T> c1, Collection<? extends T> c2) {
    return equalsCollection(c1, c2, false);
  }

  /**
   * @param c1
   * @param c2
   * @param considerElementPosition
   * @return true if the two collections contains the same elements if considerElementPosition in the same order.
   */
  public static <T> boolean equalsCollection(Collection<? extends T> c1, Collection<? extends T> c2, boolean considerElementPosition) {
    if (c1 == c2) {
      return true;
    }
    if (c1 == null || c2 == null) {
      return false;
    }
    if (c1.size() != c2.size()) {
      return false;
    }
    if (considerElementPosition) {
      Iterator<? extends T> it1 = c1.iterator();
      Iterator<? extends T> it2 = c2.iterator();
      while (it1.hasNext()) {
        if (!CompareUtility.equals(it1.next(), it2.next())) {
          return false;
        }
      }
      return true;
    }
    else {
      return c1.containsAll(c2);
    }
  }

  /**
   * compares the two lists of same content in the same order. Is an overloaded of (
   * {@link #equalsCollection(Collection, Collection)}.
   * 
   * @param c1
   * @param c2
   * @return true if the two lists contains the same elements in the same order.
   */
  public static <T> boolean equalsCollection(List<? extends T> c1, List<? extends T> c2) {
    if (c1 == c2) {
      return true;
    }
    if (c1 == null || c2 == null) {
      return false;
    }
    if (c1.size() != c2.size()) {
      return false;
    }
    return c1.equals(c2);
  }

  /*
   * Collection/List handling
   */
  public static <T> T firstElement(Collection<T> c) {
    if (isEmpty(c)) {
      return null;
    }
    return c.iterator().next();
  }

  public static <T> T firstElement(List<T> c) {
    if (isEmpty(c)) {
      return null;
    }
    return c.get(0);
  }

  public static <T> T lastElement(List<T> c) {
    if (isEmpty(c)) {
      return null;
    }
    return c.get(c.size() - 1);
  }

  /**
   * @param c
   * @param values
   * @return <code>true</code> if the collection contains one of the values.
   */
  public static <T> boolean containsAny(Collection<T> c, T... values) {
    if (values == null) {
      return false;
    }
    HashSet<T> set = hashSet(c);
    for (T value : values) {
      if (set.contains(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * exception safe access of an element of a list by index.
   * 
   * @param list
   * @param index
   * @return null if the index is out of the list bounds or the element is null by itself.
   */
  public static <T> T getElement(List<T> list, int index) {
    if (index >= 0 && index < list.size()) {
      return list.get(index);
    }
    return null;
  }

  /**
   * List factory
   */
  public static <T> ArrayList<T> arrayList(T... values) {
    if (values != null) {
      ArrayList<T> list = new ArrayList<T>(values.length);
      for (T v : values) {
        list.add(v);
      }
      return list;
    }
    return new ArrayList<T>(0);
  }

  public static <T> ArrayList<T> arrayList(T value) {
    ArrayList<T> list = new ArrayList<T>();
    if (value != null) {
      list.add(value);
    }
    return list;
  }

  public static <T> ArrayList<T> truncateList(List<? extends T> input, int maxSize) {
    if (input == null) {
      input = new ArrayList<T>();
    }
    int endIndex = Math.min(input.size(), maxSize);
    ArrayList<T> result = new ArrayList<T>(endIndex);
    for (int i = 0; i < endIndex; i++) {
      result.add(input.get(i));
    }
    return result;
  }

  /**
   * Null safe creation of a {@link ArrayList} out of a given collection. The returned {@link ArrayList} is modifiable
   * and not null.
   * 
   * @param c
   * @return an {@link ArrayList} containing the given collection's elements. Never null.
   */
  public static <T> ArrayList<T> arrayList(Collection<? extends T> c) {
    if (c != null) {
      return new ArrayList<T>(c);
    }
    return new ArrayList<T>(0);
  }

  /**
   * Null safe creation of a {@link ArrayList} out of a given collection. The returned {@link ArrayList} is modifiable.
   * The result list is never null and does not contain any null elements.
   * 
   * @param c
   * @return an {@link ArrayList} containing the given collection's elements. Never null
   */
  public static <T> ArrayList<T> arrayListWithoutNullElements(Collection<? extends T> c) {
    if (c != null) {
      ArrayList<T> list = new ArrayList<T>(c.size());
      for (T o : c) {
        if (o != null) {
          list.add(o);
        }
      }
      return list;
    }
    return new ArrayList<T>(0);
  }

  /**
   * Null safe creation of a {@link HashSet} out of a given collection. The returned {@link HashSet} is modifiable and
   * never null.
   * 
   * @param c
   * @return an {@link HashSet} containing the given collection's elements. Never null.
   */
  public static <T> HashSet<T> hashSet(Collection<? extends T> c) {
    if (c != null) {
      return new HashSet<T>(c);
    }
    return new HashSet<T>(0);
  }

  /**
   * Null safe creation of a {@link HashSet} out of a given collection without <code>null</code> elements. The returned
   * {@link HashSet} is modifiable and never null.
   * 
   * @param c
   * @return an {@link HashSet} containing the given collection's elements without <code>null</code> elements. Never
   *         null.
   */
  public static <T> HashSet<T> hashSetWithoutNullElements(Collection<? extends T> c) {
    HashSet<T> set = hashSet(c);
    set.remove(null);
    return set;
  }

  /**
   * Null safe creation of a {@link LinkedHashSet} out of a given collection. The returned {@link LinkedHashSet} is
   * modifiable and
   * never null.
   * 
   * @param c
   * @return an {@link LinkedHashSet} containing the given collection's elements. Never null.
   */
  public static <T> LinkedHashSet<T> orderedHashSet(Collection<? extends T> c) {
    if (c != null) {
      return new LinkedHashSet<T>(c);
    }
    return new LinkedHashSet<T>(0);
  }

  /**
   * Null safe creation of a {@link LinkedHashSet} out of a given collection without <code>null</code> elements. The
   * returned {@link LinkedHashSet} is modifiable and never null.
   * 
   * @param c
   * @return an {@link LinkedHashSet} containing the given collection's elements without <code>null</code> elements.
   *         Never
   *         null.
   */
  public static <T> LinkedHashSet<T> orderedHashSetWithoutNullElements(Collection<? extends T> c) {
    LinkedHashSet<T> set = orderedHashSet(c);
    set.remove(null);
    return set;
  }

  /**
   * @deprecated Will be removed in Scout 5.0. Use {@link #arrayList(Collection)} instead.
   */
  @Deprecated
  public static <T> List<T> copyList(Collection<T> c) {
    return arrayList(c);
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Collection<T> c, Class<T> clazz) {
    if (c == null || c.size() == 0) {
      T[] a = (T[]) java.lang.reflect.Array.newInstance(clazz, 0);
      return Collections.<T> emptyList().toArray(a);
    }
    else {
      T[] a = (T[]) java.lang.reflect.Array.newInstance(clazz, c.size());
      return c.toArray(a);
    }
  }

  /**
   * @deprecated Will be removed in Scout 5.0. Use {@link CollectionUtility#arrayList(Collection)} instead.
   */
  @Deprecated
  public static <T> List<T> toList(Collection<T> c) {
    if (c == null) {
      return null;
    }
    else if (c instanceof List) {
      return (List<T>) c;
    }
    else {
      return new ArrayList<T>(c);
    }
  }

  public static <T, V extends T> List<T> appendList(List<T> list, V o) {
    if (list == null) {
      list = new ArrayList<T>(1);
    }
    list.add(o);
    return list;
  }

  public static <T, V extends T> List<T> appendList(List<T> list, int index, V o) {
    if (list == null) {
      list = new ArrayList<T>(1);
    }
    if (index > list.size()) {
      for (int i = list.size(); i < index; i++) {
        list.add(i, null);
      }
    }
    list.add(index, o);
    return list;
  }

  public static <T> List<T> appendAllList(List<T> list, Collection<? extends T> c) {
    if (list == null) {
      list = new ArrayList<T>(1);
    }
    if (c != null && c.size() > 0) {
      list.addAll(c);
    }
    return list;
  }

  public static <T, V extends T> List<T> removeObjectList(List<T> list, V o) {
    if (list == null) {
      list = new ArrayList<T>(1);
    }
    list.remove(o);
    return list;
  }

  public static <T> List<T> removeObjectList(List<T> list, int i) {
    if (list == null) {
      list = new ArrayList<T>(1);
    }
    list.remove(i);
    return list;
  }

  public static <T, V extends T> Set<T> removeObjectSet(Set<T> set, V o) {
    if (set == null) {
      set = new HashSet<T>(1);
    }
    set.remove(o);
    return set;
  }

  public static <T> int size(Collection<T> list) {
    if (list == null) {
      return 0;
    }
    return list.size();
  }

  /*
   * Default Map handling
   */

  @SuppressWarnings("unchecked")
  public static <T, U> U[] getValueArray(Map<T, U> m, Class<U> clazz) {
    if (m == null || m.size() == 0) {
      U[] a = (U[]) java.lang.reflect.Array.newInstance(clazz, 0);
      return Collections.<U> emptyList().toArray(a);
    }
    else {
      U[] a = (U[]) java.lang.reflect.Array.newInstance(clazz, m.size());
      return m.values().toArray(a);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T, U> T[] getKeyArray(Map<T, U> m, Class<T> clazz) {
    if (m == null || m.size() == 0) {
      T[] a = (T[]) java.lang.reflect.Array.newInstance(clazz, 0);
      return Collections.<T> emptyList().toArray(a);
    }
    else {
      T[] a = (T[]) java.lang.reflect.Array.newInstance(clazz, m.size());
      return m.keySet().toArray(a);
    }
  }

  public static <T, U> Map<T, U> copyMap(Map<T, U> m) {
    if (m == null || m.size() == 0) {
      return new HashMap<T, U>();
    }
    else {
      return new HashMap<T, U>(m);
    }
  }

  public static <T, U, V extends T, W extends U> Map<T, U> putObject(Map<T, U> map, V key, W value) {
    if (map == null) {
      map = new HashMap<T, U>();
    }
    map.put(key, value);
    return map;
  }

  public static <T, U> U getObject(Map<T, U> map, T key) {
    if (map == null) {
      map = new HashMap<T, U>();
    }
    return map.get(key);
  }

  public static <T, U> Map<T, U> removeObject(Map<T, U> map, T key) {
    if (map == null) {
      map = new HashMap<T, U>();
    }
    map.remove(key);
    return map;
  }

  public static <T, U> boolean containsValue(Map<T, U> map, U value) {
    if (map == null) {
      return false;
    }
    return map.containsValue(value);
  }

  public static <T, U> boolean containsKey(Map<T, U> map, T key) {
    if (map == null) {
      return false;
    }
    return map.containsKey(key);
  }

  public static <T, U> Map<T, U> putAllObjects(Map<T, U> targetMap, Map<T, U> sourceMap) {
    if (targetMap == null) {
      targetMap = new HashMap<T, U>();
    }
    targetMap.putAll(sourceMap);
    return targetMap;
  }

  public static <T, U> Map<T, U> getEmptyMap(Map<T, U> m) {
    return new HashMap<T, U>();
  }

  /*
   * Sort Map handling
   */

  @SuppressWarnings("unchecked")
  public static <T, U> U[] getSortedValueArray(SortedMap<T, U> m, Class<U> clazz) {
    if (m == null || m.size() == 0) {
      U[] a = (U[]) java.lang.reflect.Array.newInstance(clazz, 0);
      return Collections.<U> emptyList().toArray(a);
    }
    else {
      U[] a = (U[]) java.lang.reflect.Array.newInstance(clazz, m.size());
      return m.values().toArray(a);
    }
  }

  public static <T, U> SortedMap<T, U> copySortedMap(SortedMap<T, U> m) {
    if (m == null || m.size() == 0) {
      return new TreeMap<T, U>();
    }
    else {
      return new TreeMap<T, U>(m);
    }
  }

  public static <T extends Comparable, U> SortedMap<T, U> putObjectSortedMap(SortedMap<T, U> map, T key, U value) {
    if (map == null) {
      map = new TreeMap<T, U>();
    }
    map.put(key, value);
    return map;
  }

  public static <T extends Comparable, U> U getObjectSortedMap(SortedMap<T, U> map, T key) {
    if (map == null) {
      map = new TreeMap<T, U>();
    }
    return map.get(key);
  }

  public static <T extends Comparable, U> SortedMap<T, U> removeObjectSortedMap(SortedMap<T, U> map, T key) {
    if (map == null) {
      map = new TreeMap<T, U>();
    }
    map.remove(key);
    return map;
  }

  public static <T extends Comparable, U> SortedMap<T, U> putAllObjectsSortedMap(SortedMap<T, U> targetMap, Map<T, U> sourceMap) {
    if (targetMap == null) {
      targetMap = new TreeMap<T, U>();
    }
    targetMap.putAll(sourceMap);
    return targetMap;
  }

  public static <T, U> SortedMap<T, U> getEmptySortedMap(SortedMap<T, U> m) {
    return new TreeMap<T, U>();
  }

  public static <T, U> U lastElement(SortedMap<T, U> m) {
    if (m == null || m.isEmpty()) {
      return null;
    }
    return m.get(m.lastKey());
  }

  public static <T, U> U firstElement(SortedMap<T, U> m) {
    if (m == null || m.isEmpty()) {
      return null;
    }
    return m.get(m.firstKey());
  }

  /**
   * Set factory
   */
  public static <T> HashSet<T> hashSet(T... values) {
    if (values != null) {
      HashSet<T> set = new HashSet<T>(values.length);
      for (T v : values) {
        set.add(v);
      }
      return set;
    }
    return new HashSet<T>(0);
  }

  public static <T> HashSet<T> hashSet(T value) {
    HashSet<T> set = new HashSet<T>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  /**
   * null safe wrapper of {@link Collections#unmodifiableCollection(Collection)}
   * 
   * @param collection
   * @return a unmodifiable collection of the given input. Never null.
   * @see {@link Collections#unmodifiableCollection(Collection)}
   */
  public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> collection) {
    if (collection != null) {
      return Collections.unmodifiableCollection(collection);
    }
    return Collections.emptyList();
  }

  /**
   * null safe wrapper of {@link Collections#unmodifiableSet(Set)}
   * 
   * @param set
   * @return a unmodifiable set containing the given input. Never null.
   * @see {@link Collections#unmodifiableSet(Set)}
   */
  public static <T> Set<T> unmodifiableSet(Set<? extends T> set) {
    if (set != null) {
      return Collections.unmodifiableSet(set);
    }
    return Collections.emptySet();
  }

  /**
   * to create an unmodifiable copy of the input set.
   * 
   * @param set
   * @return an unmodifiable copy of the input set. If the input set is null an empty unmodifiable set gets returned.
   */
  public static <T> Set<T> unmodifiableSetCopy(Collection<? extends T> set) {
    return Collections.unmodifiableSet(hashSet(set));
  }

  /**
   * null safe wrapper of {@link Collections#unmodifiableList(List)}
   * 
   * @param list
   * @return an unmodifiable list containing the given input. Never null.
   * @see {@link Collections#unmodifiableList(List)}
   */
  public static <T> List<T> unmodifiableList(List<? extends T> list) {
    if (list != null) {
      return Collections.unmodifiableList(list);
    }
    return Collections.emptyList();
  }

  /**
   * to create an unmodifiable copy of the input list.
   * 
   * @param list
   * @return an unmodifiable copy of the input list. If the input list is null an empty unmodifiable list gets returned.
   */
  public static <T> List<T> unmodifiableListCopy(Collection<? extends T> list) {
    return Collections.unmodifiableList(arrayList(list));
  }

  public static boolean isEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }

  public static boolean hasElements(Collection<?> c) {
    return !isEmpty(c);
  }

  public static <T> boolean hasElements(T[] array) {
    if (array == null) {
      return false;
    }
    return array.length > 0;
  }

  public static int hashCode(Collection<?> c) {
    if (c == null) {
      return 0;
    }
    return Arrays.hashCode(c.toArray());
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
    return CollectionUtility.arrayList(list);
  }

  /**
   * @see #format(Collection, String, boolean)
   */
  public static <T> String format(Collection<T> list, String delimiter) {
    return format(list, delimiter, false);
  }

  /**
   * @see #format(Collection, String, boolean)
   */
  public static String format(Collection<?> list) {
    return format(list, false);
  }

  /**
   * @see #format(Collection, String, boolean)
   */
  public static <T> String format(Collection<T> c, boolean quoteStrings) {
    return format(c, ", ", quoteStrings);
  }

  /**
   * To get a string representation of a collection.
   * 
   * @param list
   *          input list
   * @param delimiter
   *          the separator used between elements
   * @param quoteStrings
   *          <code>true</code> to get quoted representations of all not numbers.
   * @return a string representation of the given collection.
   */
  public static <T> String format(Collection<T> list, String delimiter, boolean quoteStrings) {
    if (isEmpty(list)) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    Iterator<T> it = list.iterator();
    // first
    result.append(objectToString(it.next(), quoteStrings));
    // rest
    while (it.hasNext()) {
      result.append(delimiter);
      result.append(objectToString(it.next(), quoteStrings));
    }
    return result.toString();
  }

  private static String objectToString(Object o, boolean quoteStrings) {
    if (o == null) {
      return "null";
    }
    if (o instanceof Number) {
      return o.toString();
    }
    if (quoteStrings) {
      return "'" + o.toString().replaceAll(",", "%2C") + "'";
    }
    else {
      return o.toString();
    }

  }
}
