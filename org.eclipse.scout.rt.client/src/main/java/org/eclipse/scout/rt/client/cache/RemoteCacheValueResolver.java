/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.cache;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
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
