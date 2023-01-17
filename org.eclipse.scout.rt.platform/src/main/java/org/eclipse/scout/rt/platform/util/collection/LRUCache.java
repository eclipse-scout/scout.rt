/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.collection;

import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.event.FastListenerList;

/**
 * Default implementation of a last recently used cache mechanism. This implementation is thread-safe.
 * {@link #setTargetSize(int)} is the number of items remaining in the cache, superfluous items are discarded and
 * notified by the DisposeListener.
 * <p>
 * <b>Consider using directly {@link ConcurrentExpiringMap}</b>
 *
 * @see ConcurrentExpiringMap
 */
@SuppressWarnings("squid:S2160")
public class LRUCache<K, V> extends ConcurrentExpiringMap<K, V> {
  private final FastListenerList<LRUCache.DisposeListener> m_listenerList = new FastListenerList<>();

  public LRUCache(int targetSize, long timeToLive) {
    super(timeToLive, TimeUnit.MILLISECONDS, targetSize);
  }

  @Override
  public Set<K> keySet() {
    // old implementation did return an new independent set
    return new HashSet<>(super.keySet());
  }

  @Override
  public Collection<V> values() {
    // old implementation did return an new independent set
    return new HashSet<>(super.values());
  }

  @Override
  protected void execEntryEvicted(K key, V value) {
    fireValueDisposed(key, value);
  }

  @Override
  public V remove(Object key) {
    V value = super.remove(key);
    if (value != null) {
      fireValueDisposed(key, value);
    }
    return value;
  }

  @Override
  public boolean remove(Object key, Object value) {
    boolean removed = super.remove(key, value);
    if (removed) {
      fireValueDisposed(key, value);
    }
    return removed;
  }

  @Override
  public V replace(K key, V value) {
    V oldValue = super.replace(key, value);
    if (oldValue != null) {
      fireValueDisposed(key, oldValue);
    }
    return oldValue;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    boolean replaced = super.replace(key, oldValue, newValue);
    if (replaced) {
      fireValueDisposed(key, oldValue);
    }
    return replaced;
  }

  @Override
  public void clear() {
    for (Entry<K, V> entry : entrySet()) {
      // we must use remove(K, V) in order to call fireValueDisposed(K, V) safely
      remove(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Dispose observer
   */
  @FunctionalInterface
  public interface DisposeListener extends EventListener {
    void valueDisposed(Object key, Object value);
  }

  public FastListenerList<LRUCache.DisposeListener> disposeListeners() {
    return m_listenerList;
  }

  public void addDisposeListener(DisposeListener listener) {
    disposeListeners().add(listener);
  }

  public void removeDisposeListener(DisposeListener listener) {
    disposeListeners().remove(listener);
  }

  private void fireValueDisposed(Object key, Object value) {
    disposeListeners().list().forEach(listener -> listener.valueDisposed(key, value));
  }
}
