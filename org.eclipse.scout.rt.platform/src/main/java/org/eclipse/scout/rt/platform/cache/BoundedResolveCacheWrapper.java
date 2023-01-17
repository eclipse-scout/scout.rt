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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;

/**
 * This wrapper bounds the maximum concurrent resolve operation of a cache. In case a waiting operation is interrupted,
 * the interrupt state on the thread is set again and the resolve is then done <b>without</b> a bound.
 * <p>
 * <b>The cache implementation on which this wrapper is based on must be still thread safe.</b>
 *
 * @since 5.2
 */
public class BoundedResolveCacheWrapper<K, V> extends AbstractCacheWrapper<K, V> {
  private final Semaphore m_semaphore;

  public BoundedResolveCacheWrapper(ICache<K, V> delegate, int maximumResolves) {
    super(delegate);
    m_semaphore = new Semaphore(maximumResolves);
  }

  @Override
  public V get(K key) {
    V value = getUnmodifiableMap().get(key);
    if (value != null) {
      return value;
    }
    try {
      m_semaphore.acquire();
    }
    catch (InterruptedException e) {
      // interrupted, mark thread again as interrupted and resolve without a semaphore anyway
      Thread.currentThread().interrupt();
      throw new ThreadInterruptedError("Interrupted during acquire", e);
    }
    try {
      return super.get(key);
    }
    finally {
      m_semaphore.release();
    }
  }

  @Override
  public Map<K, V> getAll(Collection<? extends K> keys) {
    Map<K, V> cacheMap = getUnmodifiableMap();
    Map<K, V> result = new HashMap<>();
    for (K key : keys) {
      V value = cacheMap.get(key);
      if (value == null) {
        // not all keys can be looked up without resolve. call super anyway
        break;
      }
      result.put(key, cacheMap.get(key));
    }
    if (result.size() == keys.size()) {
      return result;
    }
    try {
      m_semaphore.acquire();
    }
    catch (InterruptedException e) {
      // interrupted, mark thread again as interrupted and resolve without a semaphore anyway
      Thread.currentThread().interrupt();
      return super.getAll(keys);
    }
    try {
      return super.getAll(keys);
    }
    finally {
      m_semaphore.release();
    }
  }
}
