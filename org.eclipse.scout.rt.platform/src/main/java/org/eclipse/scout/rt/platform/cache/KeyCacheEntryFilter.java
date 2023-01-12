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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Special filter that accepts entries according to a set of keys.
 * <p>
 * This class is immutable.
 *
 * @since 5.2
 */
public final class KeyCacheEntryFilter<K, V> implements ICacheEntryFilter<K, V> {
  private static final long serialVersionUID = 1L;
  private final Set<K> m_keys;

  public KeyCacheEntryFilter(Collection<? extends K> keys) {
    m_keys = Collections.unmodifiableSet(CollectionUtility.hashSetWithoutNullElements(keys));
  }

  public Set<K> getKeys() {
    return m_keys;
  }

  @Override
  public boolean accept(K key, V value) {
    return m_keys.contains(key);
  }

  @Override
  public ICacheEntryFilter<K, V> coalesce(ICacheEntryFilter<K, V> other) {
    if (other instanceof KeyCacheEntryFilter) {
      HashSet<K> newKeySet = new HashSet<>(m_keys);
      newKeySet.addAll(((KeyCacheEntryFilter<K, V>) other).m_keys);
      return new KeyCacheEntryFilter<>(newKeySet);
    }
    return null;
  }

  @Override
  public String toString() {
    return "KeyCacheEntryFilter [m_keys=" + m_keys + ']';
  }
}
