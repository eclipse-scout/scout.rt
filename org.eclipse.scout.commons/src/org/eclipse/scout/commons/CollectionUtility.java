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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public final class CollectionUtility {
  private CollectionUtility() {
  }

  /*
   * Collection/List handling
   */
  public static <T> T firstElement(Collection<T> c) {
    if (c == null || c.size() == 0) {
      return null;
    }
    else {
      return c.iterator().next();
    }
  }

  public static <T> T lastElement(List<T> c) {
    if (c == null || c.size() == 0) {
      return null;
    }
    else {
      return c.get(c.size() - 1);
    }
  }

  public static <T> List<T> copyList(Collection<T> c) {
    if (c == null || c.size() == 0) {
      return new ArrayList<T>(0);
    }
    else {
      return new ArrayList<T>(c);
    }
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

  public static <T> Set<T> removeObjectSet(Set<T> set, int i) {
    if (set == null) {
      set = new HashSet<T>(1);
    }
    set.remove(i);
    return set;
  }

  public static <T> int size(List<T> list) {
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
    HashSet<T> set = new HashSet<T>();
    if (values != null) {
      for (T v : values) {
        set.add(v);
      }
    }
    return set;
  }
}
