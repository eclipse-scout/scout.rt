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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of a time to live cache mechanism.
 * <p>
 * <b>Consider using directly {@link ConcurrentExpiringMap}</b>
 *
 * @see ConcurrentExpiringMap
 */
public class TTLCache<K, V> extends ConcurrentExpiringMap<K, V> {

  public TTLCache() {
    super();
  }

  /**
   * @param timeToLive
   *          in milliseconds
   */
  public TTLCache(long timeToLive) {
    super(timeToLive, TimeUnit.MILLISECONDS);
  }

  /**
   * @deprecated use {@link #getTimeToLive()} instead
   */
  @Deprecated
  public long getTTL() {
    return getTimeToLive();
  }

  /**
   * @deprecated timeToLive is now final in order to be thread safe. If you need to change ttl of a
   *             {@link ConcurrentExpiringMap} simply create a new {@link ConcurrentExpiringMap} using a
   *             copy-constructor: <br>
   *             {@code m_ttlMap = new ConcurrentExpiringMap(m_ttlMap, newTimeToLive, timeToLiveUnit);} Important:
   *             m_ttlMap must be volatile or be guarded by a lock.
   */
  @Deprecated
  public void setTTL(long millis) {
  }

  @Deprecated
  public Map<K, V> getEntries() {
    return new HashMap<>(this);
  }

  @Deprecated
  public boolean contains(K key, V value) {
    V currentValue = get(key);
    return currentValue == value || (currentValue != null && currentValue.equals(value));
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
}
