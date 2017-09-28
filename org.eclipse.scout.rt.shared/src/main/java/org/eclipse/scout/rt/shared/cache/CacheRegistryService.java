/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * A registry allowing to register and retrieving caches with a given id.
 */
public class CacheRegistryService implements ICacheRegistryService {

  private final Map<String, ICache> m_map = new ConcurrentHashMap<>();

  /**
   * Register a cache with a given id
   */
  @Override
  public <K, V> void register(ICache<K, V> cache) {
    getMap().put(cache.getCacheId(), cache);
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
