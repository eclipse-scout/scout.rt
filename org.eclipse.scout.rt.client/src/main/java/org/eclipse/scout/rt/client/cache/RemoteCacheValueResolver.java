/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.cache;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;

/**
 * Used when a client cache is based on a server cache.
 *
 * @since 5.2
 */
public class RemoteCacheValueResolver<K, V> implements ICacheValueResolver<K, V> {
  private final String m_cacheId;

  public RemoteCacheValueResolver(String cacheId) {
    super();
    m_cacheId = cacheId;
  }

  public String getCacheId() {
    return m_cacheId;
  }

  @SuppressWarnings("unchecked")
  @Override
  public V resolve(K key) {
    return (V) BEANS.get(IRemoteCacheService.class).get(getCacheId(), key);
  }

  @Override
  public Map<K, V> resolveAll(Set<K> keys) {
    return BEANS.get(IRemoteCacheService.class).getAll(getCacheId(), keys);
  }
}
