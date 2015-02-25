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
package org.eclipse.scout.net.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TTLCache<K, V> {
  private Map<K, CacheEntryTTL> m_itemMap;
  private long m_ttl;
  private final Object m_itemMapLock = new Object();

  public TTLCache() {
    this(60000);// 60 sec. default
  }

  public TTLCache(long timeToLive) {
    m_ttl = timeToLive;
    m_itemMap = new HashMap<K, CacheEntryTTL>();
  }

  public void clear() {
    synchronized (m_itemMapLock) {
      m_itemMap.clear();
    }
  }

  public void setTTL(long millis) {
    m_ttl = millis;
  }

  public long getTTL() {
    return m_ttl;
  }

  public void put(K key, V value) {
    if (m_ttl > 0) {
      synchronized (m_itemMapLock) {
        m_itemMap.put(key, new CacheEntryTTL(value));
      }
    }
  }

  public void remove(K key) {
    synchronized (m_itemMapLock) {
      m_itemMap.remove(key);
    }
  }

  public V get(K key) {
    CacheEntryTTL ce = getEntry(key);
    if (ce != null) {
      return ce.getValue();
    }
    else {
      return null;
    }
  }

  public Set<K> keySet() {
    HashSet<K> list = new HashSet<K>();
    synchronized (m_itemMapLock) {
      for (Iterator<K> it = m_itemMap.keySet().iterator(); it.hasNext();) {
        K key = it.next();
        if (!isCacheTimeoutReached(m_itemMap.get(key))) {
          list.add(key);
        }
        else {
          it.remove();
        }
      }
    }
    return list;
  }

  public int size() {
    synchronized (m_itemMapLock) {
      return m_itemMap.size();
    }
  }

  public Collection<V> values() {
    ArrayList<V> list = new ArrayList<V>();
    synchronized (m_itemMapLock) {
      for (Iterator<CacheEntryTTL> it = m_itemMap.values().iterator(); it.hasNext();) {
        CacheEntryTTL cacheEntry = it.next();
        if (!isCacheTimeoutReached(cacheEntry)) {
          list.add(cacheEntry.getValue());
        }
        else {
          it.remove();
        }
      }
    }
    return list;
  }

  public Map<K, V> getEntries() {
    Map<K, V> map = new HashMap<K, V>();
    synchronized (m_itemMapLock) {
      for (Iterator<Entry<K, CacheEntryTTL>> it = m_itemMap.entrySet().iterator(); it.hasNext();) {
        Entry<K, CacheEntryTTL> e = it.next();
        if (!isCacheTimeoutReached(e.getValue())) {
          map.put(e.getKey(), e.getValue().getValue());
        }
        else {
          it.remove();
        }
      }
    }
    return map;
  }

  public boolean containsKey(K key) {
    if (m_ttl > 0) {
      CacheEntryTTL ce = getEntry(key);
      if (ce != null) {
        return true;
      }
    }
    return false;
  }

  public boolean contains(K key, V value) {
    if (m_ttl > 0) {
      CacheEntryTTL ce = getEntry(key);
      if (ce != null) {
        V ceVal = ce.getValue();
        if (ceVal == value || (ceVal != null && ceVal.equals(value))) {
          // ok
          return true;
        }
      }
    }
    return false;
  }

  private CacheEntryTTL getEntry(K key) {
    if (m_ttl > 0) {
      CacheEntryTTL ce;
      synchronized (m_itemMapLock) {
        ce = m_itemMap.get(key);
        if (ce != null) {
          if (!isCacheTimeoutReached(ce)) {
            return ce;
          }
          else {
            // timeout
            m_itemMap.remove(key);
          }
        }
      }
    }
    return null;
  }

  private boolean isCacheTimeoutReached(CacheEntryTTL value) {
    return value.getTimestamp() + m_ttl <= System.currentTimeMillis();
  }

  private class CacheEntryTTL {
    private long m_time = System.currentTimeMillis();
    private V m_value;

    public CacheEntryTTL(V value) {
      m_value = value;
    }

    public long getTimestamp() {
      return m_time;
    }

    public V getValue() {
      return m_value;
    }
  }// end class

}
