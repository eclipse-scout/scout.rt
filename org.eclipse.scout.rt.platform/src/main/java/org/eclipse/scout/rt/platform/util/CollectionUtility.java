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
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public final class CollectionUtility {
  private CollectionUtility() {
  }

  /**
   * Compares the two collection of same content in any order.
   * <p>
   * Note: This method is overloaded ({@link #equalsCollection(List, List)} for lists where the order of the list is
   * considered. If the order of lists should be ignored, use {@link #equalsCollection(Collection, Collection, boolean)}
   * and pass <code>false</code> as last argument.
   *
   * @param c1
   *          first collection
   * @param c2
   *          second collection
   * @return <code>true</code> if the two collections contains the same elements <i>in any order</i>.
   */
  public static <T> boolean equalsCollection(Collection<? extends T> c1, Collection<? extends T> c2) {
    return equalsCollection(c1, c2, false);
  }

  /**
   * @param c1
   *          first collection
   * @param c2
   *          second collection
   * @param considerElementPosition
   *          If <code>true</code>, the order of the elements in the two collections must match. If <code>false</code>,
   *          the order is not relevant (only the elements must be the same).
   * @return <code>true</code> if the two collections contains the same elements <i>in any <b>or</b> the same order</i>
   *         (depending on the value of 'considerElementPosition').
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
      Map<T, AtomicInteger> histogram = new HashMap<>();
      for (T e1 : c1) {
        AtomicInteger counter = histogram.get(e1);
        if (counter == null) {
          histogram.put(e1, new AtomicInteger(1));
        }
        else {
          counter.incrementAndGet();
        }
      }

      for (T e2 : c2) {
        AtomicInteger counter = histogram.get(e2);
        if (counter == null) {
          return false;
        }
        else if (counter.decrementAndGet() == 0) {
          histogram.remove(e2);
        }
      }

      return histogram.isEmpty();
    }
  }

  /**
   * compares the two lists of same content in the same order. Is an overloaded of (
   * {@link #equalsCollection(Collection, Collection)}.
   *
   * @param c1
   *          first list
   * @param c2
   *          second list
   * @return <code>true</code> if the two lists contains the same elements <i>in the same order</i>.
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

  @SuppressWarnings("unchecked")
  public static <T> T firstElement(Object c) {
    if (c instanceof Collection<?>) {
      return (T) firstElement((Collection<?>) c);
    }
    return null;
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
   * Returns a sub list with the provided indices. This methods supports negative indices in the sense that -1 addresses
   * the last element and -2 the 2nd last. For positive indices, the methods uses
   * {@link java.util.List#subList(int, int)} with one change: It is returning the sub list starting with c[fromIndex]
   * to c[toIndex]. slice(c, 0, 0): first element of c slice(c, 0, -3): c without the last two elements
   *
   * @param c
   * @param fromIndex
   * @param toIndex
   * @return
   */
  public static <T> List<T> slice(List<T> c, int fromIndex, int toIndex) {
    List<T> result = new ArrayList<T>();

    // null check
    if (c == null) {
      return result;
    }

    int len = c.size();

    // arguments check
    if (fromIndex > len || toIndex > len || fromIndex < -len || toIndex < -len) {
      throw new IndexOutOfBoundsException("fromIndex or toIndex out of bounds");
    }

    // special case for empty list
    if (len > 0 && fromIndex >= len) {
      throw new IndexOutOfBoundsException("fromIndex or toIndex out of bounds");
    }

    // map negative indices
    if (fromIndex < 0) {
      fromIndex += len;
    }

    if (toIndex < 0) {
      toIndex += len + 1;
    }
    else if (toIndex == 0 && len > 0 || toIndex > 0) {
      toIndex++;
    }

    return new ArrayList<T>(c.subList(fromIndex, toIndex));
  }

  /**
   * @param c
   * @param values
   * @return <code>true</code> if the collection contains one of the values.
   */
  public static <T> boolean containsAny(Collection<T> c, Collection<? extends T> values) {
    if (values == null || c == null) {
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
   * @param c
   * @param values
   * @return <code>true</code> if the collection contains one of the values.
   */
  @SafeVarargs
  public static <T> boolean containsAny(Collection<T> c, T... values) {
    if (values == null || c == null) {
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
  @SafeVarargs
  public static <T> ArrayList<T> arrayList(T... values) {
    if (values == null) {
      return emptyArrayList();
    }
    ArrayList<T> list = new ArrayList<T>(values.length);
    for (T v : values) {
      list.add(v);
    }
    return list;
  }

  /**
   * Returns a new empty {@link ArrayList}.<br>
   * This method differs to {@link Collections#emptyList()} in that way that the {@link ArrayList} returned by this
   * method can be modified hence is no shared instance.
   *
   * @return An empty but modifiable {@link ArrayList} with an initial capacity of <code>0</code>.
   */
  public static <T> ArrayList<T> emptyArrayList() {
    return new ArrayList<T>(0);
  }

  public static <T> ArrayList<T> arrayList(T value) {
    ArrayList<T> list = new ArrayList<T>(1);
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
    return emptyArrayList();
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
    return emptyArrayList();
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
   * modifiable and never null.
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
   *         Never null.
   */
  public static <T> LinkedHashSet<T> orderedHashSetWithoutNullElements(Collection<? extends T> c) {
    LinkedHashSet<T> set = orderedHashSet(c);
    set.remove(null);
    return set;
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

  public static <T, V extends T> List<T> appendList(List<T> list, V o) {
    if (list == null) {
      list = new ArrayList<T>();
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
      return new HashMap<T, U>(0);
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
      return null;
    }
    return map.get(key);
  }

  public static <T, U> Map<T, U> removeObject(Map<T, U> map, T key) {
    if (map == null) {
      return new HashMap<T, U>();
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
    if (targetMap == null && sourceMap == null) {
      return new HashMap<T, U>();
    }
    if (targetMap == null) {
      return new HashMap<T, U>(sourceMap);
    }
    if (sourceMap == null) {
      return targetMap; // nothing to add
    }
    targetMap.putAll(sourceMap);
    return targetMap;
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
      return null;
    }
    return map.get(key);
  }

  public static <T extends Comparable, U> SortedMap<T, U> removeObjectSortedMap(SortedMap<T, U> map, T key) {
    if (map == null) {
      return new TreeMap<T, U>();
    }
    map.remove(key);
    return map;
  }

  public static <T extends Comparable, U> SortedMap<T, U> putAllObjectsSortedMap(SortedMap<T, U> targetMap, Map<T, U> sourceMap) {
    if (targetMap == null && sourceMap == null) {
      return new TreeMap<T, U>();
    }
    if (targetMap == null) {
      return new TreeMap<T, U>(sourceMap);
    }
    if (sourceMap == null) {
      return targetMap; // nothing to add
    }
    targetMap.putAll(sourceMap);
    return targetMap;
  }

  /**
   * Returns a new empty {@link HashMap}.<br>
   * This method differs to {@link Collections#emptyMap()} in that way that the {@link HashMap} returned by this method
   * can be modified hence is no shared instance.
   *
   * @return An empty but modifiable {@link HashMap} with an initial capacity of <code>0</code>.
   */
  public static <T, U> HashMap<T, U> emptyHashMap() {
    return new HashMap<T, U>(0);
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
  @SafeVarargs
  public static <T> HashSet<T> hashSet(T... values) {
    if (values == null) {
      return emptyHashSet();
    }
    HashSet<T> set = new HashSet<T>(values.length);
    for (T v : values) {
      set.add(v);
    }
    return set;
  }

  public static <T> HashSet<T> hashSet(T value) {
    HashSet<T> set = new HashSet<T>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  /**
   * Returns a new empty {@link HashSet}.<br>
   * This method differs to {@link Collections#emptyList()} in that way that the {@link HashSet} returned by this method
   * can be modified hence is no shared instance.
   *
   * @return An empty but modifiable {@link HashSet} with an initial capacity of <code>0</code>.
   */
  public static <T> HashSet<T> emptyHashSet() {
    return new HashSet<T>(0);
  }

  /**
   * Returns a new empty {@link TreeSet}.<br>
   * This method differs to {@link Collections#emptySet()} in that way that the {@link TreeSet} returned by this method
   * can be modified hence is no shared instance.
   *
   * @return An empty but modifiable {@link TreeSet} with an initial capacity of <code>0</code>.
   */
  public static <T> TreeSet<T> emptyTreeSet() {
    return new TreeSet<T>();
  }

  /**
   * combine all lists into one list containing all elements. the order of the items is preserved
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> combine(Collection<?>... collections) {
    List<T> list = new ArrayList<T>();
    if (collections != null && collections.length > 0) {
      for (Collection<?> c : collections) {
        for (Object t : c) {
          list.add((T) t);
        }
      }
    }
    return list;
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

  // TODO [5.2] jgu: ???
  public static List<Object> parse(String text) {
    List<Object> list = null;
    if (StringUtility.hasText(text)) {
      String[] a = text.split(",");
      for (String s : a) {
        Object o;
        // remove escaped ','
        s = s.replaceAll("%2C", ",");
        if ("null".equalsIgnoreCase(s)) {
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
            o = Long.valueOf(s);
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
