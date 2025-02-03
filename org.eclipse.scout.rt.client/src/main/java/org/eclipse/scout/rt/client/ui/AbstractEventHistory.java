/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;

/**
 * The events are kept in history for at most n milliseconds using a {@link ConcurrentExpiringMap}
 * <p>
 * This object is thread safe.
 *
 * @since 3.8
 */
public abstract class AbstractEventHistory<T> implements IEventHistory<T> {
  private final ConcurrentExpiringMap<Object, T> m_cache;

  public AbstractEventHistory(long timeToLiveMillis) {
    m_cache = new ConcurrentExpiringMap<>(timeToLiveMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * override and call {@link #addToCache(Object, Object)} for relevant events
   */
  @Override
  public abstract void notifyEvent(T event);

  /**
   * Add event to cache map using the key. The key is used to manage single or multi-event of same type
   */
  protected void addToCache(Object uniquenessKey, T event) {
    m_cache.put(uniquenessKey, event);
  }

  @Override
  public Collection<T> getRecentEvents() {
    return m_cache.values();
  }
}
