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
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Interface to model a cache. In case of a cache miss, the functional interface
 * {@link ICacheValueResolver#resolve(Object)} is called to fetch or recompute the value.
 * <p>
 * If one needs to reload one or multiple keys, simply invalidate first the keys and then call {@link #get(Object)} or
 * {@link #getAll(Collection)}
 * <p>
 * Any null key or values are <em>not</em> allowed. They are ignored.
 * <p>
 * Any other additional features of a cache should be implemented using {@link AbstractCacheWrapper}. This way, those
 * features can be exchanged easily.
 * <p>
 * Use {@link ICacheBuilder} to create a cache instance. Each cache instance is registered as {@link Bean}.
 *
 * @param <K>
 *          the type of keys maintained by this cache
 * @param <V>
 *          the type of mapped values
 * @see ICacheValueResolver
 * @see ICacheBuilder
 * @see AbstractCacheWrapper
 * @see BasicCache
 * @since 5.2
 */
public interface ICache<K, V> extends IAdaptable {

  /**
   * A static unique id used to identify the cache.
   */
  String getCacheId();

  /**
   * Cache label.
   */
  String getLabel();

  /**
   * First, tries to lookup the value in the cache. In case of a cache miss, {@link ICacheValueResolver#resolve(Object)}
   * is called to fetch or recompute the value. The value is then stored in the local cache.
   *
   * @param key
   *          if null, null is returned.
   */
  V get(K key);

  /**
   * Like {@link #get(Object)} but it calls {@link ICacheValueResolver#resolveAll(Set)} on all cache misses in order to
   * allow batch resolves.
   *
   * @return never null
   */
  Map<K, V> getAll(Collection<? extends K> keys);

  /**
   * Invalidate cache entries corresponding to the given {@link ICacheEntryFilter}.
   *
   * @param filter
   *          used to match entries that should be invalidated
   * @param propagate
   *          true if the invalidation should be propagated to other nodes. In case of a client cache, the invalidation
   *          is propagated to the server. In case of a server cache the invalidation is propagated to other server
   *          cluster nodes. Note that a server propagates invalidations always to its connected clients.
   */
  void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate);

  /**
   * @return cached value for given key if or null if not yet in cache
   */
  V getCachedValue(K key);

  /**
   * @return new map with the current content of the cache
   */
  Map<K, V> getCacheMap();

  /**
   * @return an unmodifiable view of the map on which this cache is based on. Never null.
   */
  Map<K, V> getUnmodifiableMap();

}
