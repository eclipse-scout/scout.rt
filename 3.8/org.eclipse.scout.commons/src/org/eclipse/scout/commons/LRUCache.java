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
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Default implementation of a last recently used cache mechanism using a HashMap. This implementation is thread-safe.
 * {@link #setTargetSize(int)} is the number of items remaining in the cache, superfluous items are discarded and
 * notified by the DisposeListener
 */
public class LRUCache<K, V> {
  private long m_nextSecondarySeq = 1;
  private int m_targetSize = 1000;
  private int m_overflowSize;
  private long m_timeout = -1;
  private HashMap<K, CacheEntry> m_accessMap;
  private final Object m_accessMapLock = new Object();
  private final EventListenerList m_listenerList = new EventListenerList();

  public LRUCache(int targetSize, long timeout) {
    m_accessMap = new HashMap<K, CacheEntry>();
    setTargetSize(targetSize);
    setTimeout(timeout);
  }

  public V get(K key) {
    boolean fire = false;
    V fireValue = null;
    V returnValue = null;
    synchronized (m_accessMapLock) {
      CacheEntry ce = m_accessMap.get(key);
      if (ce != null) {
        if (ce.isTimeout()) {
          m_accessMap.remove(key);
          fire = true;
          fireValue = ce.getValue();
          ce = null;
        }
      }
      if (ce != null) {
        ce.touch();
        returnValue = ce.getValue();
      }
      else {
        returnValue = null;
      }
    }
    if (fire) {
      fireValueDisposed(key, fireValue);
    }
    return returnValue;
  }

  public void put(K key, V value) {
    if (m_targetSize <= 0) {
      return;
    }
    boolean fire = false;
    V fireValue = null;
    //
    synchronized (m_accessMapLock) {
      validateCacheSizeNoLock();
      CacheEntry ce = m_accessMap.get(key);
      if (ce != null) {
        V oldValue = ce.getValue();
        ce.updateValue(value);
        if (oldValue != value) {
          fire = true;
          fireValue = oldValue;
        }
      }
      else {
        ce = new CacheEntry(key, value);
        m_accessMap.put(key, ce);
      }
    }
    if (fire) {
      fireValueDisposed(key, fireValue);
    }
  }

  public void remove(K key) {
    if (m_targetSize <= 0) {
      return;
    }
    boolean fire = false;
    V fireValue = null;
    //
    synchronized (m_accessMapLock) {
      CacheEntry ce = m_accessMap.remove(key);
      if (ce != null) {
        fire = true;
        fireValue = ce.getValue();
      }
    }
    if (fire) {
      fireValueDisposed(key, fireValue);
    }
  }

  public void clear() {
    Map<K, CacheEntry> oldMap;
    synchronized (m_accessMapLock) {
      oldMap = m_accessMap;
      m_accessMap = new HashMap<K, CacheEntry>();
    }
    for (CacheEntry ce : oldMap.values()) {
      fireValueDisposed(ce.getKey(), ce.getValue());
    }
  }

  public void setTimeout(long timeout) {
    m_timeout = timeout;
  }

  public long getTimeout() {
    return m_timeout;
  }

  public void setTargetSize(int size) {
    m_targetSize = size;
    m_overflowSize = m_targetSize * 3 / 2;
  }

  public int getTargetSize() {
    return m_targetSize;
  }

  public Map<K, V> entries() {
    return getCacheContent();
  }

  public Map<K, V> getCacheContent() {
    synchronized (m_accessMapLock) {
      HashMap<K, V> map = new HashMap<K, V>();
      for (CacheEntry ce : m_accessMap.values()) {
        if (!ce.isTimeout()) {
          map.put(ce.getKey(), ce.getValue());
        }
      }
      return map;
    }
  }

  public Set<K> keySet() {
    synchronized (m_accessMapLock) {
      HashSet<K> set = new HashSet<K>(m_accessMap.size());
      for (CacheEntry ce : m_accessMap.values()) {
        if (!ce.isTimeout()) {
          set.add(ce.getKey());
        }
      }
      return set;
    }
  }

  public boolean containsKey(K key) {
    synchronized (m_accessMapLock) {
      return m_accessMap.containsKey(key);
    }
  }

  public Collection<V> values() {
    synchronized (m_accessMapLock) {
      ArrayList<V> list = new ArrayList<V>(m_accessMap.size());
      for (CacheEntry ce : m_accessMap.values()) {
        if (!ce.isTimeout()) {
          list.add(ce.getValue());
        }
      }
      return list;
    }
  }

  public boolean containsValue(V value) {
    synchronized (m_accessMapLock) {
      for (CacheEntry ce : m_accessMap.values()) {
        if (ce.getValue() == null && value == null) {
          return true;
        }
        else if (ce.getValue() != null && ce.getValue().equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  private void validateCacheSizeNoLock() {
    if (m_accessMap.size() >= m_overflowSize) {
      TreeSet<CacheEntry> sortSet = new TreeSet<CacheEntry>(new CacheEntryTimestampComparator());
      for (CacheEntry ce : m_accessMap.values()) {
        sortSet.add(ce);
      }
      // rebuild access map with a maximum of targetSize elements
      m_accessMap.clear();
      int count = 0;
      for (CacheEntry ce : sortSet) {
        if (count > m_targetSize || ce.isTimeout()) {
          fireValueDisposed(ce.getKey(), ce.getValue());
        }
        else {
          m_accessMap.put(ce.getKey(), ce);
          count++;
        }
      }
    }
  }

  /**
   * Cache Entry Value Object
   */
  private class CacheEntry {
    private final K m_key;
    private V m_value;
    private long m_timestamp;
    private Long m_secondarySeq;

    public CacheEntry(K key, V value) {
      m_key = key;
      m_value = value;
      touch();
    }

    public K getKey() {
      return m_key;
    }

    public V getValue() {
      return m_value;
    }

    public long getTimestamp() {
      return m_timestamp;
    }

    public boolean isTimeout() {
      return m_timeout > 0 && m_timestamp + m_timeout < System.currentTimeMillis();
    }

    public void updateValue(V newValue) {
      m_value = newValue;
      touch();
    }

    public void touch() {
      m_timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
      return "LRU[" + m_key + "," + m_value + "]";
    }
  }// end class

  private class CacheEntryTimestampComparator implements Comparator<CacheEntry> {

    @Override
    public int compare(CacheEntry ca, CacheEntry cb) {
      if (ca.m_timestamp < cb.m_timestamp) {
        return +1;
      }
      if (ca.m_timestamp > cb.m_timestamp) {
        return -1;
      }
      // equal timestamps
      if (ca.m_secondarySeq == null) {
        ca.m_secondarySeq = (m_nextSecondarySeq++);
      }
      if (cb.m_secondarySeq == null) {
        cb.m_secondarySeq = (m_nextSecondarySeq++);
      }
      return ca.m_secondarySeq.compareTo(cb.m_secondarySeq);
    }
  }// end class

  /**
   * Dispose observer
   */
  public interface DisposeListener extends EventListener {
    void valueDisposed(Object key, Object value);
  }

  public void addDisposeListener(DisposeListener listener) {
    m_listenerList.add(DisposeListener.class, listener);
  }

  public void removeDisposeListener(DisposeListener listener) {
    m_listenerList.remove(DisposeListener.class, listener);
  }

  private void fireValueDisposed(Object key, Object value) {
    EventListener[] a = m_listenerList.getListeners(DisposeListener.class);
    for (int i = 0; i < a.length; i++) {
      ((DisposeListener) a[i]).valueDisposed(key, value);
    }
  }
}
