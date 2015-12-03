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
package org.eclipse.scout.rt.shared.cache;

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
