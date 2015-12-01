/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Basic implementation of {@link ICache}.
 * <p>
 * Note:
 * <ul>
 * <li>This cache is only thread safe if the provided cacheMap is thread safe.
 * <li>There might be multiple concurrent resolve operations running for the same key unless one is using
 * {@link BoundedResolveCacheWrapper} to limit these operations.
 * </ul>
 *
 * @since 5.2
 */
public class BasicCache<K, V> implements ICache<K, V> {

  private final String m_cacheId;
  private final ICacheValueResolver<K, V> m_resolver;
  private final Map<K, V> m_cacheMap;
  private final Map<K, V> m_unmodifiableMap;
  private final boolean m_atomicInsertion;

  public BasicCache(String cacheId, ICacheValueResolver<K, V> resolver, Map<K, V> cacheMap, boolean atomicInsertion) {
    super();
    if (cacheId == null || resolver == null || cacheMap == null) {
      throw new NullPointerException();
    }
    m_cacheId = cacheId;
    m_resolver = resolver;
    m_cacheMap = cacheMap;
    m_unmodifiableMap = Collections.unmodifiableMap(m_cacheMap);
    m_atomicInsertion = atomicInsertion;
    if (m_atomicInsertion && !(cacheMap instanceof ConcurrentMap)) {
      throw new IllegalArgumentException("To use atomic insertions cacheMap must implement ConcurrentMap interface");
    }
  }

  public ICacheValueResolver<K, V> getResolver() {
    return m_resolver;
  }

  public Map<K, V> getCacheMap() {
    return m_cacheMap;
  }

  @Override
  public String getCacheId() {
    return m_cacheId;
  }

  @Override
  public Map<K, V> getUnmodifiableMap() {
    return m_unmodifiableMap;
  }

  @Override
  public V get(K key) {
    if (key == null) {
      return null;
    }
    V value = m_cacheMap.get(key);
    if (value == null) {
      try {
        value = m_resolver.resolve(key);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException(String.format("Failed resolving key: %s", key), e));
      }
      if (value != null) {
        if (m_atomicInsertion) {
          V alreadySetValue = ((ConcurrentMap<K, V>) m_cacheMap).putIfAbsent(key, value);
          value = alreadySetValue != null ? alreadySetValue : value;
        }
        else {
          m_cacheMap.put(key, value);
        }
      }
    }
    return value;
  }

  @Override
  public Map<K, V> getAll(Collection<? extends K> keys0) {
    Set<K> keys = CollectionUtility.hashSetWithoutNullElements(keys0);
    if (keys.isEmpty()) {
      return CollectionUtility.emptyHashMap();
    }
    Map<K, V> result = new HashMap<K, V>();
    for (Iterator<K> iterator = keys.iterator(); iterator.hasNext();) {
      K key = iterator.next();
      V value = m_cacheMap.get(key);
      if (value != null) {
        result.put(key, value);
        iterator.remove();
      }
    }
    Map<K, V> resolvedValues;
    try {
      resolvedValues = m_resolver.resolveAll(keys);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Failed resolving keys: %s", CollectionUtility.format(keys), e));
      return result;
    }
    for (Iterator<Entry<K, V>> iterator = resolvedValues.entrySet().iterator(); iterator.hasNext();) {
      Entry<K, V> entry = iterator.next();
      // remove any null values from the resolved values map
      if (entry.getKey() == null || entry.getValue() == null) {
        iterator.remove();
      }
      else if (m_atomicInsertion) {
        V alreadySetValue = ((ConcurrentMap<K, V>) m_cacheMap).putIfAbsent(entry.getKey(), entry.getValue());
        if (alreadySetValue != null) {
          entry.setValue(alreadySetValue);
        }
      }
    }
    if (!m_atomicInsertion) {
      m_cacheMap.putAll(resolvedValues);
    }
    result.putAll(resolvedValues);
    return result;
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    if (filter instanceof AllCacheEntryFilter) {
      m_cacheMap.clear();
    }
    else if (filter instanceof KeyCacheEntryFilter) {
      KeyCacheEntryFilter<K, V> keyCacheEntryFilter = (KeyCacheEntryFilter<K, V>) filter;
      for (K key : keyCacheEntryFilter.getKeys()) {
        m_cacheMap.remove(key);
      }
    }
    else if (filter != null) {
      for (Iterator<Entry<K, V>> iterator = m_cacheMap.entrySet().iterator(); iterator.hasNext();) {
        Entry<K, V> entry = iterator.next();
        if (filter.accept(entry.getKey(), entry.getValue())) {
          iterator.remove();
        }
      }
    }
  }

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    return null;
  }
}
