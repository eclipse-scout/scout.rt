/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.cache;

import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheRegistryService;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;

/**
 * @since 5.2
 */
public class RemoteCacheService implements IRemoteCacheService {

  @Override
  public <K, V> V get(String cacheId, K key) {
    ICache<K, V> cache = getCache(cacheId);
    if (cache != null) {
      return cache.get(key);
    }
    return null;
  }

  @Override
  public <K, V> Map<K, V> getAll(String cacheId, Collection<? extends K> keys) {
    ICache<K, V> cache = getCache(cacheId);
    if (cache != null) {
      return cache.getAll(keys);
    }
    return CollectionUtility.emptyHashMap();
  }

  @Override
  public <K, V> void invalidate(String cacheId, ICacheEntryFilter<K, V> filter, boolean propagate) {
    ICache<K, V> cache = getCache(cacheId);
    if (cache != null) {
      cache.invalidate(filter, propagate);
    }
  }

  protected <K, V> ICache<K, V> getCache(String cacheId) {
    return BEANS.get(ICacheRegistryService.class).get(cacheId);
  }
}
