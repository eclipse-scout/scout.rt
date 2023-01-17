/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.cache;

import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Service to register caches with a given id and access a cache with a given id on the server.
 *
 * @see ICache
 * @since 5.2
 */
@ApplicationScoped
@TunnelToServer
public interface IRemoteCacheService {

  /**
   * Like {@link ICache#get(Object)} it gets a value from the cache with the given cacheId.
   *
   * @param cacheId
   * @param key
   * @return null if cache can not be found, key is null or cache resolver returns null for the key
   */
  <K, V> V get(String cacheId, K key);

  /**
   * Like {@link ICache#getAll(Collection)} it gets a values from the cache with the given cacheId.
   *
   * @param cacheId
   * @param keys
   * @return never null
   */
  <K, V> Map<K, V> getAll(String cacheId, Collection<? extends K> keys);

  /**
   * Like {@link ICache#invalidate(ICacheEntryFilter, boolean)} it invalidates cache entries from the cache with the
   * given cacheId.
   *
   * @param cacheId
   * @param filter
   *          used to match entries that should be invalidated
   * @param propagate
   *          true if the invalidation should be propagated to other cluster nodes
   */
  <K, V> void invalidate(String cacheId, ICacheEntryFilter<K, V> filter, boolean propagate);

}
