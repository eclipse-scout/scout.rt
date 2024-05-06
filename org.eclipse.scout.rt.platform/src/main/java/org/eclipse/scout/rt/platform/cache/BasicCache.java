/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.AbstractTransactionalMap;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;

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

  protected final String m_cacheId;
  protected final Supplier<String> m_labelSupplier;
  protected final ICacheValueResolver<K, V> m_resolver;
  protected final Map<K, V> m_cacheMap;

  protected final AbstractTransactionalMap<K, ?> m_transactionalMap; // is null if not transactional cache

  /**
   * @deprecated Use constructor including label supplier as second argument. Label supplier can be retrieved via
   * {@link CacheBuilder#getLabelSupplier()}.
   */
  @Deprecated(forRemoval = true)
  public BasicCache(String cacheId, ICacheValueResolver<K, V> resolver, Map<K, V> cacheMap) {
    this(cacheId, null, resolver, cacheMap, findTransactionalMap(cacheMap));
  }

  public BasicCache(String cacheId, Supplier<String> labelSupplier, ICacheValueResolver<K, V> resolver, Map<K, V> cacheMap) {
    this(cacheId, labelSupplier, resolver, cacheMap, findTransactionalMap(cacheMap));
  }

  @SuppressWarnings("unchecked")
  private static <K, V> AbstractTransactionalMap<K, ?> findTransactionalMap(Map<K, V> cacheMap) {
    Map<K, ?> innerMap = cacheMap;
    if (cacheMap instanceof ConcurrentExpiringMap) {
      innerMap = ((ConcurrentExpiringMap<K, Object>) cacheMap).getElementMap();
    }
    if (innerMap instanceof AbstractTransactionalMap) {
      return (AbstractTransactionalMap<K, ?>) innerMap;
    }
    return null;
  }

  /**
   * @deprecated Use constructor including label supplier as second argument. Label supplier can be retrieved via
   *             {@link CacheBuilder#getLabelSupplier()}.
   */
  @Deprecated(forRemoval = true)
  public BasicCache(String cacheId, ICacheValueResolver<K, V> resolver, Map<K, V> cacheMap, AbstractTransactionalMap<K, ?> transactionalMap) {
    this(cacheId, null, resolver, cacheMap, transactionalMap);
  }

  public BasicCache(String cacheId, Supplier<String> labelSupplier, ICacheValueResolver<K, V> resolver, Map<K, V> cacheMap, AbstractTransactionalMap<K, ?> transactionalMap) {
    m_cacheId = Assertions.assertNotNullOrEmpty(cacheId);
    m_labelSupplier = labelSupplier;
    m_resolver = Assertions.assertNotNull(resolver);
    m_cacheMap = Assertions.assertNotNull(cacheMap);

    m_transactionalMap = transactionalMap;
  }

  @Override
  public String getCacheId() {
    return m_cacheId;
  }

  @Override
  public String getLabel() {
    return m_labelSupplier == null ? null : m_labelSupplier.get();
  }

  @Override
  public V getCachedValue(K key) {
    return m_cacheMap.get(key);
  }

  @Override
  public Map<K, V> getCacheMap() {
    return new HashMap<>(m_cacheMap);
  }

  @Override
  public Map<K, V> getUnmodifiableMap() {
    return Collections.unmodifiableMap(m_cacheMap); //DO NOT cache this, because entrySet/keySet/values are lazily initialized and only once.
  }

  @Override
  public V get(K key) {
    if (key == null) {
      return null;
    }
    V value = m_cacheMap.get(key);
    if (value == null) {
      if (m_transactionalMap != null) {
        m_transactionalMap.getTransactionMember(true); // enforce creation of transaction member before resolve
      }
      value = m_resolver.resolve(key);
      if (value != null) {
        V alreadySetValue = m_cacheMap.putIfAbsent(key, value);
        value = alreadySetValue != null ? alreadySetValue : value;
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
    Map<K, V> result = new HashMap<>();
    for (Iterator<K> iterator = keys.iterator(); iterator.hasNext();) {
      K key = iterator.next();
      V value = m_cacheMap.get(key);
      if (value != null) {
        result.put(key, value);
        iterator.remove();
      }
    }
    if (keys.isEmpty()) {
      // all keys could be resolved with cache
      return result;
    }

    if (m_transactionalMap != null) {
      m_transactionalMap.getTransactionMember(true); // enforce creation of transaction member before resolve
    }
    Map<K, V> resolvedValues = m_resolver.resolveAll(keys);
    for (Iterator<Entry<K, V>> iterator = resolvedValues.entrySet().iterator(); iterator.hasNext();) {
      Entry<K, V> entry = iterator.next();
      // remove any null values from the resolved values map
      if (entry.getKey() == null || entry.getValue() == null) {
        iterator.remove();
      }
      else {
        V alreadySetValue = m_cacheMap.putIfAbsent(entry.getKey(), entry.getValue());
        if (alreadySetValue != null) {
          entry.setValue(alreadySetValue);
        }
      }
    }
    result.putAll(resolvedValues);
    return result;
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    boolean markInsertsDirty = true;

    if (filter instanceof AllCacheEntryFilter) {
      m_cacheMap.clear();
    }
    else if (filter instanceof KeyCacheEntryFilter) {
      markInsertsDirty = false; // if all remove operations find a previous value, we do not need to mark inserts of other transactions as dirty
      KeyCacheEntryFilter<K, V> keyCacheEntryFilter = (KeyCacheEntryFilter<K, V>) filter;
      for (K key : keyCacheEntryFilter.getKeys()) {
        boolean valueNotRemoved = m_cacheMap.remove(key) == null;
        markInsertsDirty = markInsertsDirty | valueNotRemoved;
      }
    }
    else if (filter != null) {
      m_cacheMap.entrySet().removeIf(entry -> filter.accept(entry.getKey(), entry.getValue()));
    }

    if (markInsertsDirty && m_transactionalMap != null) {
      m_transactionalMap.markInsertsDirty();
    }
  }

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    return null;
  }
}
