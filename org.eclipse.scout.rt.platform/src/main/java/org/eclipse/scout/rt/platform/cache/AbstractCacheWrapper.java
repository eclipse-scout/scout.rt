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

/**
 * Base wrapper class for {@link ICache}.
 *
 * @since 5.2
 */
public abstract class AbstractCacheWrapper<K, V> implements ICache<K, V> {
  private final ICache<K, V> m_delegate;

  public AbstractCacheWrapper(ICache<K, V> delegate) {
    m_delegate = delegate;
  }

  protected ICache<K, V> getDelegate() {
    return m_delegate;
  }

  @Override
  public String getCacheId() {
    return m_delegate.getCacheId();
  }

  @Override
  public String getLabel() {
    return m_delegate.getLabel();
  }

  @Override
  public Map<K, V> getCacheMap() {
    return m_delegate.getCacheMap();
  }

  @Override
  public V getCachedValue(K key) {
    return m_delegate.getCachedValue(key);
  }

  @Override
  public Map<K, V> getUnmodifiableMap() {
    return m_delegate.getUnmodifiableMap();
  }

  @Override
  public V get(K key) {
    return m_delegate.get(key);
  }

  @Override
  public Map<K, V> getAll(Collection<? extends K> keys) {
    return m_delegate.getAll(keys);
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    m_delegate.invalidate(filter, propagate);
  }

  @Override
  public <T> T getAdapter(Class<T> adapterClass) {
    return m_delegate.getAdapter(adapterClass);
  }
}
