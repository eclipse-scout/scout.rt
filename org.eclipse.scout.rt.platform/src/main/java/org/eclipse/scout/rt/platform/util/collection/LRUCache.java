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
package org.eclipse.scout.rt.platform.util.collection;

import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.EventListenerList;

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
  private final EventListenerList m_listenerList = new EventListenerList();

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
    for (Iterator<Entry<K, V>> iterator = entrySet().iterator(); iterator.hasNext();) {
      Entry<K, V> entry = iterator.next();
      // we must use remove(K, V) in order to call fireValueDisposed(K, V) safely
      remove(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Dispose observer
   */
  public interface DisposeListener extends EventListener {
    void valueDisposed(Object key, Object value);
  }

  public void addDisposeListener(DisposeListener listener) {
    m_listenerList.add(DisposeListener.class, listener);
  }

  public void removeDisposeListener(DisposeListener listener) {
    m_listenerList.remove(DisposeListener.class, listener);
  }

  private void fireValueDisposed(Object key, Object value) {
    EventListener[] a = m_listenerList.getListeners(DisposeListener.class);
    for (int i = 0; i < a.length; i++) {
      ((DisposeListener) a[i]).valueDisposed(key, value);
    }
  }
}
