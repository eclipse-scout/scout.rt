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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * A registry allowing to register and retrieving caches with a given id.
 */
public class CacheRegistryService implements ICacheRegistryService {

  private final Map<String, ICache> m_map = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> ICache<K, V> registerIfAbsent(ICache<K, V> cache) {
    return (ICache<K, V>) getMap().computeIfAbsent(cache.getCacheId(), id -> cache);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> void registerAndReplace(ICache<K, V> cache) {
    getMap().put(cache.getCacheId(), cache);
  }

  @Override
  public <K, V> void register(ICache<K, V> cache) {
    Assertions.assertNull(getMap().putIfAbsent(cache.getCacheId(), cache), "The cache with id '{}' is already registered. Consider calling registerIfAbsent instead.", cache.getCacheId());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> ICache<K, V> get(String cacheId) {
    return (ICache<K, V>) Assertions.assertNotNull(opt(cacheId));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> ICache<K, V> opt(String cacheId) {
    return getMap().get(cacheId);
  }

  protected Map<String, ICache> getMap() {
    return m_map;
  }
}
