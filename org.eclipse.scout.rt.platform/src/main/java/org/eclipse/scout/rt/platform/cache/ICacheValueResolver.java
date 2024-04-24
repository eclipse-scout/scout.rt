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
import java.util.stream.Collectors;

/**
 * In case of a cache miss, a value resolver is called to fetch or recompute the value corresponding to the given key.
 *
 * @see ICache
 * @since 5.2
 */
public interface ICacheValueResolver<K, V> {

  /**
   * @param key
   *          never null
   */
  V resolve(K key);

  /**
   * If the returned map contains additional keys not present in parameter <code>keys</code>, then all returned entries
   * will be cached and all the entries will be returned by {@link ICache#getAll(Collection)}.
   *
   * @param keys
   *          never null, empty or contains null elements
   * @return non null modifiable map
   */
  default Map<K, V> resolveAll(Set<K> keys) {
    return keys.stream().collect(Collectors.toMap(key -> key, this::resolve));
  }
}
