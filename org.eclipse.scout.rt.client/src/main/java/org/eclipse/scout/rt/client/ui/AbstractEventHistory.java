/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    m_cache = new ConcurrentExpiringMap<Object, T>(timeToLiveMillis, TimeUnit.MILLISECONDS);
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
