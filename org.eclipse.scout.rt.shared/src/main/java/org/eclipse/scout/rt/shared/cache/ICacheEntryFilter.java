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

import java.io.Serializable;

/**
 * Filter applied to {@link ICache} entries.
 *
 * @param <K>
 *          the type of keys maintained by the cache
 * @param <V>
 *          the type of mapped values in the cache
 * @since 5.2
 */
public interface ICacheEntryFilter<K, V> extends Serializable {

  /**
   * @param key
   *          of an entry; not null
   * @param value
   *          of the same entry; not null
   * @return true if the filter matches
   */
  boolean accept(K key, V value);

  /**
   * Merge with another cache entry filter. This function is in general <b>not</b> symmetric.
   *
   * @param other
   *          filter to merge with
   * @return null if this filter can not be merged with the other filter. Else a (maybe new) filter is returned so that
   *         this new filter matches the same entries as the old filter and additionally matches the entries from the
   *         other filter.
   */
  ICacheEntryFilter<K, V> coalesce(ICacheEntryFilter<K, V> other);
}
