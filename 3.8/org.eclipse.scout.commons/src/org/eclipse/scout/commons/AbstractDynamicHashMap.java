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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Implementation of a time-dynamic map that supports the dynamic removal of items due to a dynamic condition.
 * <p>
 * This is a generalization of for example {@link WeakHashMap} that discards items due to garbage collection.
 * <p>
 * The condition that controls keep/discard of contained items can be applied by overriding the method
 * {@link #isEntryValid(DynamicEntry)}.
 * <p>
 * You may use {@link #touch(Object)} to update the cache state of an entry
 */
public abstract class AbstractDynamicHashMap<K, V> implements Map<K, V>, Serializable {
  private static final long serialVersionUID = 1L;

  protected final Long internalMapLock;
  protected final Map<K, DynamicEntry<V>> internalMap;

  public AbstractDynamicHashMap() {
    internalMapLock = new Long(1L);
    internalMap = new HashMap<K, DynamicEntry<V>>();
  }

  /**
   * This method is called inside the internal map lock before every access to the internal map.
   * <p>
   * This may (but needs not) update the map. It can depend on performance or other optimization strategies.
   */
  protected void beforeAccessToInternalMap() {
  }

  /**
   * verifies all entries in the internal map and makes it valid
   */
  protected void validateInternalMap() {
    for (Iterator<Map.Entry<K, DynamicEntry<V>>> it = internalMap.entrySet().iterator(); it.hasNext();) {
      Map.Entry<K, DynamicEntry<V>> e = it.next();
      DynamicEntry<V> entry = e.getValue();
      if (!isEntryValid(entry)) {
        it.remove();
        removeNotify(entry);
      }
    }
  }

  /**
   * creates a {@link DynamicEntry} or a subclass of it
   */
  protected DynamicEntry<V> createDynamicEntry(V value) {
    return new DynamicEntry<V>(value);
  }

  /**
   * this method is called when an entry is removed
   */
  protected void removeNotify(DynamicEntry<V> entry) {
  }

  /**
   * This method is called inside the internal map lock whenever a dynamic entry has been accessed. It is therefore safe
   * to manipulate and change the contents of
   * the internal map
   */
  protected abstract boolean isEntryValid(DynamicEntry<V> e);

  @Override
  public V get(Object key) {
    synchronized (internalMapLock) {
      beforeAccessToInternalMap();
      DynamicEntry<V> entry = internalMap.get(key);
      if (entry != null && isEntryValid(entry)) {
        return entry.getValue();
      }
      return null;
    }
  }

  @Override
  public V put(K key, V value) {
    synchronized (internalMapLock) {
      beforeAccessToInternalMap();
      DynamicEntry<V> entry = internalMap.put(key, createDynamicEntry(value));
      if (entry != null && isEntryValid(entry)) {
        return entry.getValue();
      }
      return null;
    }
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    synchronized (internalMapLock) {
      beforeAccessToInternalMap();
      for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
        internalMap.put(e.getKey(), createDynamicEntry(e.getValue()));
      }
    }
  }

  public V touch(K key) {
    synchronized (internalMapLock) {
      beforeAccessToInternalMap();
      DynamicEntry<V> entry = internalMap.get(key);
      if (entry != null) {
        entry.touch();
        return entry.getValue();
      }
      return null;
    }
  }

  @Override
  public boolean containsKey(Object key) {
    synchronized (internalMapLock) {
      beforeAccessToInternalMap();
      DynamicEntry<V> entry = internalMap.get(key);
      if (entry != null && isEntryValid(entry)) {
        return true;
      }
      return false;
    }
  }

  @Override
  public boolean containsValue(Object value) {
    synchronized (internalMapLock) {
      validateInternalMap();
      for (Map.Entry<K, DynamicEntry<V>> e : internalMap.entrySet()) {
        Object existingValue = e.getValue().getValue();
        if (value == existingValue || (value != null && value.equals(existingValue))) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public V remove(Object key) {
    synchronized (internalMapLock) {
      beforeAccessToInternalMap();
      DynamicEntry<V> entry = internalMap.remove(key);
      if (entry != null && isEntryValid(entry)) {
        return entry.getValue();
      }
      return null;
    }
  }

  @Override
  public void clear() {
    synchronized (internalMapLock) {
      internalMap.clear();
    }
  }

  @Override
  public int size() {
    synchronized (internalMapLock) {
      validateInternalMap();
      return internalMap.size();
    }
  }

  @Override
  public boolean isEmpty() {
    synchronized (internalMapLock) {
      validateInternalMap();
      return internalMap.isEmpty();
    }
  }

  @Override
  public Set<K> keySet() {
    synchronized (internalMapLock) {
      validateInternalMap();
      return new HashSet<K>(internalMap.keySet());
    }
  }

  @Override
  public Collection<V> values() {
    synchronized (internalMapLock) {
      validateInternalMap();
      ArrayList<V> list = new ArrayList<V>(internalMap.size());
      for (Map.Entry<K, DynamicEntry<V>> e : internalMap.entrySet()) {
        list.add(e.getValue().getValue());
      }
      return list;
    }
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    synchronized (internalMapLock) {
      validateInternalMap();
      HashMap<K, V> map = new HashMap<K, V>();
      for (Map.Entry<K, DynamicEntry<V>> e : internalMap.entrySet()) {
        map.put(e.getKey(), e.getValue().getValue());
      }
      return map.entrySet();
    }
  }

  public static class DynamicEntry<T> {
    private final T m_value;

    public DynamicEntry(T value) {
      m_value = value;
    }

    public void touch() {
    }

    public T getValue() {
      return m_value;
    }
  }

}
