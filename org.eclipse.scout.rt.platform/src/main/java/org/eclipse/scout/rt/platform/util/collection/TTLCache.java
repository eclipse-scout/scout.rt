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
import java.util.HashSet;
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
