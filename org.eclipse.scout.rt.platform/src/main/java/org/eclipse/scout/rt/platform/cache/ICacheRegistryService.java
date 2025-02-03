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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * A registry allowing to register and retrieving caches with a given id.
 */
@ApplicationScoped
public interface ICacheRegistryService {

  /**
   * Register a cache. Use {@link #get(String)} to query the registry for a cache with a given id.
   *
   * @throws AssertionException
   *     if there is already a cache with this cache id
   */
  <K, V> void register(ICache<K, V> cache);

  /**
   * Register a cache with a given id
   * <p>
   *
   * @return the current (existing or computed) cache associated with the specified cache id
   * @since 11
   */
  default <K, V> ICache<K, V> registerIfAbsent(ICache<K, V> cache) {
    throw new UnsupportedOperationException();
  }

  /**
   * Register a cache with a given id and replaces an optional existing cache with same id
   *
   * @since 11
   */
  default <K, V> void registerAndReplace(ICache<K, V> cache) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a previously registered cache
   *
   * @throws AssertionException
   *     if not found
   */
  <K, V> ICache<K, V> get(String cacheId);

  /**
   * @return a previously registered cache or <code>null</code>, if not found.
   */
  <K, V> ICache<K, V> opt(String cacheId);
}
