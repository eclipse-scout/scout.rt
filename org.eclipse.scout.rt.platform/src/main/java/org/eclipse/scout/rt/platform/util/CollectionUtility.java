/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
   *     first collection
   * @param c2
   *     second collection
   * @return <code>true</code> if the two collections contains the same elements <i>in any order</i>.
   */
  public static <T> boolean equalsCollection(Collection<? extends T> c1, Collection<? extends T> c2) {
    return equalsCollection(c1, c2, false);
  }

  /**
   * @param c1
   *     first collection
   * @param c2
   *     second collection
   * @param considerElementPosition
   *     If <code>true</code>, the order of the elements in the two collections must match. If <code>false</code>,
   *     the order is not relevant (only the elements must be the same).
   * @return <code>true</code> if the two collections contains the same elements <i>in any <b>or</b> the same order</i>
   * (depending on the value of 'considerElementPosition').
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
        if (ObjectUtility.notEquals(it1.next(), it2.next())) {
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
   * Hash code suitable for use when equalsCollection is used within an object's {@link #equals(Object)} method. This
   * method guarantees that if {@link #equalsCollection(Collection, Collection, boolean)} returns true, then this method
   * will return the same hashCode for both arguments, i.e. this method is always consistent with
   * {@link #equalsCollection}.
   *
   * @return If the size of the collection is <= 1, a hashCode equivalent to Set#hashCode will be returned. Otherwise, a
   * custom hashCode based on the histogram will be returned.
   */
  public static <T> int hashCodeCollection(Collection<? extends T> c) {
    return hashCodeCollection(c, false);
  }

  /**
   * Hash code suitable for use when equalsCollection is used within an object's {@link #equals(Object)} method. This
   * method guarantees that if {@link #equalsCollection(Collection, Collection, boolean)} returns true, then this method
   * will return the same hashCode for both arguments.
   * <p>
   * This method is always consistent with {@link #equalsCollection} if the parameter considerElementPosition is set to
   * false, otherwise it is only consistent if {@link #equalsCollection} is only ever called with
   * considerElementPosition true, or if the size of the collection is always <= 1.
   *
   * @return If the size of the collection is == 1, a hashCode equivalent to Set#hashCode will be returned. When
   * considerElementPosition is true and the size of the collection is > 1, a hashCode equivalent to
   * List#hashCode will be returned. Otherwise, a custom hashCode based on the histogram will be returned.
   * @see #equalsCollection(Collection, Collection, boolean)
   */
  public static <T> int hashCodeCollection(Collection<? extends T> c, boolean considerElementPosition) {
    if (c == null) {
      return 0;
    }

    int hashCode = 0;
    Iterator<? extends T> it = c.iterator();
    if (it.hasNext()) {
      // use Set#hashCode algorithm (see javadoc there) if size == 1.
      hashCode += Objects.hashCode(it.next());
    }
    if (!considerElementPosition) {
      // use custom algorithm (sum of all elements like Set#hashCode, but also counting duplicate items) if considerElementPosition == false
      while (it.hasNext()) {
        hashCode += Objects.hashCode(it.next());
      }
    }
    else {
      // use List#hashCode algorithm (see javadoc there) if size > 1 and considerElementPosition == true
      if (it.hasNext()) {
        hashCode += 31;
      }
      while (it.hasNext()) {
        hashCode = 31 * hashCode + Objects.hashCode(it.next());
      }
    }
    return hashCode;
  }

  /**
   * compares the two lists of same content in the same order. Is an overloaded of (
   * {@link #equalsCollection(Collection, Collection)}.
   *
   * @param c1
   *     first list
   * @param c2
   *     second list
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
   * the last element and -2 the 2nd last. For positive indices, the methods uses {@link List#subList(int, int)} with
   * one change: It is returning the sub list starting with c[fromIndex] to c[toIndex]. slice(c, 0, 0): first element of
   * c slice(c, 0, -3): c without the last two elements
   */
  public static <T> List<T> slice(List<T> c, int fromIndex, int toIndex) {
    List<T> result = new ArrayList<>();

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

    return new ArrayList<>(c.subList(fromIndex, toIndex));
  }

  /**
   * Null-safe variant of {@link Collection#contains(Object)}.
   * <p>
   * Note that this method may still throw {@link ClassCastException} or {@link NullPointerException} if the specified
   * collection does not support the given object's type.
   */
  public static <T> boolean contains(Collection<T> c, T value) {
    if (c == null) {
      return false;
    }
    return c.contains(value);
  }

  /**
   * Null-safe variant of {@link Collection#containsAll(Collection)}.
   * <p>
   * Note that this method may still throw {@link ClassCastException} or {@link NullPointerException} if the specified
   * collection does not support the given objects' types.
   */
  public static <T> boolean containsAll(Collection<T> c, Collection<? extends T> values) {
    if (c == null) {
      return false;
    }
    if (values == null) {
      return true;
    }
    return c.containsAll(values);
  }

  /**
   * This method is similar to {@link #containsAll(Collection, Collection)}, but allows passing a variable number of
   * arguments.
   *
   * @return <code>true</code> if the collection contains all of the values.
   */
  @SafeVarargs
  public static <T> boolean containsAll(Collection<T> c, T... values) {
    if (c == null) {
      return false;
    }
    if (values == null) {
      return true;
    }
    Set<T> set = hashSet(c);
    for (T value : values) {
      if (!set.contains(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return <code>true</code> if the collection contains one of the values.
   */
  public static <T> boolean containsAny(Collection<T> c, Collection<? extends T> values) {
    if (values == null || c == null || c.isEmpty() || values.isEmpty()) {
      return false;
    }
    return !Collections.disjoint(c, values);
  }

  /**
   * @return <code>true</code> if the collection contains one of the values.
   */
  @SafeVarargs
  public static <T> boolean containsAny(Collection<T> c, T... values) {
    if (values == null || values.length == 0 || c == null || c.isEmpty()) {
      return false;
    }

    return containsAny(c, arrayList(values));
  }

  /**
   * exception safe access of an element of a list by index.
   *
   * @return null if the index is out of the list bounds or the element is null by itself.
   */
  public static <T> T getElement(List<T> list, int index) {
    if (list != null && index >= 0 && index < list.size()) {
      return list.get(index);
    }
    return null;
  }

  /**
   * List factory
   */
  @SafeVarargs
  public static <T> ArrayList<T> arrayList(T... values) {
    if (values == null || values.length < 1) {
      return emptyArrayList();
    }
    ArrayList<T> list = new ArrayList<>(values.length);
    Collections.addAll(list, values);
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
    return new ArrayList<>(0);
  }

  public static <T> ArrayList<T> arrayList(T value) {
    if (value == null) {
      return emptyArrayList();
    }

    ArrayList<T> list = new ArrayList<>(1);
    list.add(value);
    return list;
  }

  public static <T> ArrayList<T> truncateList(List<? extends T> input, int maxSize) {
    if (input == null) {
      input = new ArrayList<>();
    }
    int endIndex = Math.min(input.size(), maxSize);
    ArrayList<T> result = new ArrayList<>(endIndex);
    for (int i = 0; i < endIndex; i++) {
      result.add(input.get(i));
    }
    return result;
  }

  /**
   * Null safe creation of a {@link ArrayList} out of a given collection. The returned {@link ArrayList} is modifiable
   * and not null.
   *
   * @return an {@link ArrayList} containing the given collection's elements. Never null.
   */
  public static <T> ArrayList<T> arrayList(Collection<? extends T> c) {
    if (c == null || c.isEmpty()) {
      return emptyArrayList();
    }
    return new ArrayList<>(c);
  }

  /**
   * Null safe creation of a {@link ArrayList} out of a given collection. The returned {@link ArrayList} is modifiable.
   * The result list is never null and does not contain any null elements.
   *
   * @return an {@link ArrayList} containing the given collection's elements. Never null
   */
  public static <T> ArrayList<T> arrayListWithoutNullElements(Collection<? extends T> c) {
    if (c == null || c.isEmpty()) {
      return emptyArrayList();
    }

    ArrayList<T> list = new ArrayList<>(c.size());
    for (T o : c) {
      if (o != null) {
        list.add(o);
      }
    }
    return list;
  }

  /**
   * Null safe creation of a {@link HashSet} out of a given collection. The returned {@link HashSet} is modifiable and
   * never null.
   *
   * @return an {@link HashSet} containing the given collection's elements. Never null.
   */
  public static <T> HashSet<T> hashSet(Collection<? extends T> c) {
    if (c == null || c.isEmpty()) {
      return new HashSet<>(0);
    }
    return new HashSet<>(c);
  }

  /**
   * Null safe creation of a {@link HashSet} out of a given collection without <code>null</code> elements. The returned
   * {@link HashSet} is modifiable and never null.
   *
   * @return an {@link HashSet} containing the given collection's elements without <code>null</code> elements. Never
   * null.
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
   * @return an {@link LinkedHashSet} containing the given collection's elements. Never null.
   */
  public static <T> LinkedHashSet<T> orderedHashSet(Collection<? extends T> c) {
    if (c == null || c.isEmpty()) {
      return new LinkedHashSet<>(0);
    }
    return new LinkedHashSet<>(c);
  }

  /**
   * Null safe creation of a {@link LinkedHashSet} out of a given collection without <code>null</code> elements. The
   * returned {@link LinkedHashSet} is modifiable and never null.
   *
   * @return an {@link LinkedHashSet} containing the given collection's elements without <code>null</code> elements.
   * Never null.
   */
  public static <T> LinkedHashSet<T> orderedHashSetWithoutNullElements(Collection<? extends T> c) {
    LinkedHashSet<T> set = orderedHashSet(c);
    set.remove(null);
    return set;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Collection<T> c, Class<T> clazz) {
    if (c == null || c.isEmpty()) {
      return (T[]) Array.newInstance(clazz, 0);
    }

    T[] a = (T[]) Array.newInstance(clazz, c.size());
    return c.toArray(a);
  }

  public static <T, V extends T> List<T> appendList(List<T> list, V o) {
    if (list == null) {
      list = new ArrayList<>(1);
    }
    list.add(o);
    return list;
  }

  public static <T, V extends T> List<T> appendList(List<T> list, int index, V o) {
    if (list == null) {
      list = new ArrayList<>(index + 1);
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
      list = new ArrayList<>(size(c));
    }
    if (c != null && !c.isEmpty()) {
      list.addAll(c);
    }
    return list;
  }

  public static <T, V extends T> List<T> removeObjectList(List<T> list, V o) {
    if (list == null) {
      return emptyArrayList();
    }

    list.remove(o);
    return list;
  }

  public static <T> List<T> removeObjectList(List<T> list, int i) {
    if (list == null) {
      return emptyArrayList();
    }

    list.remove(i);
    return list;
  }

  public static <T, V extends T> Set<T> removeObjectSet(Set<T> set, V o) {
    if (set == null) {
      return emptyHashSet();
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
    if (m == null || m.isEmpty()) {
      U[] a = (U[]) Array.newInstance(clazz, 0);
      return Collections.<U> emptyList().toArray(a);
    }
    else {
      U[] a = (U[]) Array.newInstance(clazz, m.size());
      return m.values().toArray(a);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T, U> T[] getKeyArray(Map<T, U> m, Class<T> clazz) {
    if (m == null || m.isEmpty()) {
      T[] a = (T[]) Array.newInstance(clazz, 0);
      return Collections.<T> emptyList().toArray(a);
    }
    else {
      T[] a = (T[]) Array.newInstance(clazz, m.size());
      return m.keySet().toArray(a);
    }
  }

  public static <T, U> Map<T, U> copyMap(Map<T, U> m) {
    if (m == null || m.isEmpty()) {
      return emptyHashMap();
    }
    return new HashMap<>(m);
  }

  public static <T, U> LinkedHashMap<T, U> copyOrderedHashMap(LinkedHashMap<T, U> m) {
    if (m == null || m.isEmpty()) {
      return emptyOrderedHashMap();
    }
    return new LinkedHashMap<>(m);
  }

  public static <T, U, V extends T, W extends U> Map<T, U> putObject(Map<T, U> map, V key, W value) {
    if (map == null) {
      map = new HashMap<>(1);
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
      return emptyHashMap();
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
      return emptyHashMap();
    }
    if (targetMap == null) {
      return new HashMap<>(sourceMap);
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
    if (m == null || m.isEmpty()) {
      return (U[]) Array.newInstance(clazz, 0);
    }

    U[] a = (U[]) Array.newInstance(clazz, m.size());
    return m.values().toArray(a);
  }

  public static <T, U> SortedMap<T, U> copySortedMap(SortedMap<T, U> m) {
    if (m == null || m.isEmpty()) {
      return new TreeMap<>();
    }
    return new TreeMap<>(m);
  }

  public static <T extends Comparable, U> SortedMap<T, U> putObjectSortedMap(SortedMap<T, U> map, T key, U value) {
    if (map == null) {
      map = new TreeMap<>();
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
      return new TreeMap<>();
    }
    map.remove(key);
    return map;
  }

  public static <T extends Comparable, U> SortedMap<T, U> putAllObjectsSortedMap(SortedMap<T, U> targetMap, Map<T, U> sourceMap) {
    if (targetMap == null && sourceMap == null) {
      return new TreeMap<>();
    }
    if (targetMap == null) {
      return new TreeMap<>(sourceMap);
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
    return new HashMap<>(0);
  }

  public static <T, U> LinkedHashMap<T, U> emptyOrderedHashMap() {
    return new LinkedHashMap<>(0);
  }

  @SafeVarargs
  public static <T, U> HashMap<T, U> hashMap(Pair<T, U>... entries) {
    if (entries == null || entries.length < 1) {
      return emptyHashMap();
    }

    HashMap<T, U> hashMap = new HashMap<>();
    for (Pair<T, U> entry : entries) {
      if (entry == null) {
        continue;
      }
      hashMap.put(entry.getLeft(), entry.getRight());
    }
    return hashMap;
  }

  @SafeVarargs
  public static <T, U> LinkedHashMap<T, U> orderedHashMap(Pair<T, U>... entries) {
    if (entries == null || entries.length < 1) {
      return emptyOrderedHashMap();
    }

    LinkedHashMap<T, U> hashMap = new LinkedHashMap<>(0);
    for (Pair<T, U> entry : entries) {
      if (entry == null) {
        continue;
      }
      hashMap.put(entry.getLeft(), entry.getRight());
    }
    return hashMap;
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
    if (values == null || values.length < 1) {
      return emptyHashSet();
    }
    HashSet<T> set = new HashSet<>(values.length);
    Collections.addAll(set, values);
    return set;
  }

  public static <T> HashSet<T> hashSet(T value) {
    if (value == null) {
      return emptyHashSet();
    }

    HashSet<T> set = new HashSet<>(1);
    set.add(value);
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
    return new HashSet<>(0);
  }

  /**
   * Returns a new empty {@link TreeSet}.<br>
   * This method differs to {@link Collections#emptySet()} in that way that the {@link TreeSet} returned by this method
   * can be modified hence is no shared instance.
   *
   * @return An empty but modifiable {@link TreeSet} with an initial capacity of <code>0</code>.
   */
  public static <T> TreeSet<T> emptyTreeSet() {
    return new TreeSet<>();
  }

  /**
   * @see #flatten(Collection)
   */
  @SafeVarargs
  public static <T> List<T> combine(Collection<? extends T>... collections) {
    return flatten(collections);
  }

  /**
   * Flattens all specified {@link Collection}s to one single {@link List}.
   * <p>
   * The order of the specified {@link Collections} is preserved and the resulting {@link List} does not contain any
   * {@code null} elements.
   *
   * @param collections
   *     The {@link Collection}s to flatten.
   * @return A {@link List} holding all non-{@code null} elements of the specified {@link Collection}s. Is never
   * {@code null}.
   * @see #combine(Collection...)
   */
  public static <T> List<T> flatten(Collection<? extends Collection<? extends T>> collections) {
    List<T> result = new ArrayList<>();
    if (collections == null) {
      return result;
    }
    for (Collection<? extends T> c : collections) {
      if (c == null) {
        continue;
      }
      for (T t : c) {
        if (t != null) {
          result.add(t);
        }
      }
    }
    return result;
  }

  /**
   * @see #flatten(Collection)
   */
  @SafeVarargs
  public static <T> List<T> flatten(Collection<? extends T>... collections) {
    return collections != null ? flatten(Arrays.asList(collections)) : null;
  }

  public static boolean isEmpty(Map<?, ?> m) {
    return m == null || m.isEmpty();
  }

  public static boolean isEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }

  public static boolean hasElements(Map<?, ?> m) {
    return !isEmpty(m);
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
    return c.hashCode();
  }

  // TODO [7.0] abr: ???
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
            o = Double.valueOf(s);
          }
          catch (NumberFormatException e) {
            o = s;
          }
        }
        else {
          // try to make long
          try {
            o = Long.valueOf(s);
          }
          catch (NumberFormatException e) {
            o = s;
          }
        }
        list = appendList(list, o);
      }
    }
    return arrayList(list);
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
   *     input list
   * @param delimiter
   *     the separator used between elements
   * @param quoteStrings
   *     <code>true</code> to get quoted representations of all not numbers.
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

  /**
   * Partition a list into fixed size of sublist.
   *
   * @param collection
   *     input collection
   * @param batchSize
   *     max size of a sublist
   * @return a list with sublist
   */
  public static <T> List<List<T>> partition(Collection<T> collection, int batchSize) {
    if (isEmpty(collection)) {
      return new ArrayList<>();
    }
    AtomicInteger counter = new AtomicInteger();
    return new ArrayList<>(collection.stream()
        .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize))
        .values());
  }

  /**
   * Returns a set of all elements in the given collection that are contained more than once in the collection. If the
   * argument is {@code null}, an empty set is returned. Elements are considered the same if they are
   * {@link Object#equals(Object) equals}. {@code null} elements are allowed.
   *
   * @param collection
   *     collection to check for duplicate values
   * @return set of duplicate values
   */
  public static <T> Set<T> findDuplicates(Collection<T> collection) {
    if (isEmpty(collection)) {
      return Collections.emptySet();
    }
    Set<T> uniqueValues = new HashSet<>();
    return collection.stream()
        .filter(e -> !uniqueValues.add(e)) // add() returns false when the element already was in the set
        .collect(Collectors.toSet());
  }
}
