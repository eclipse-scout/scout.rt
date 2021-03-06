/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.cache;

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
   * @param keys
   *          never null, empty or contains null elements
   * @return non null modifiable map
   */
  default Map<K, V> resolveAll(Set<K> keys) {
    return keys.stream().collect(Collectors.toMap(key -> key, this::resolve));
  }
}
