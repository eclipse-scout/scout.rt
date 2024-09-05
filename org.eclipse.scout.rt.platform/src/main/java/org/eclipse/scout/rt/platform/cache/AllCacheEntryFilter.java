/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.cache;

/**
 * Special filter that accepts all entries
 * <p>
 * This class is immutable.
 *
 * @since 5.2
 */
public final class AllCacheEntryFilter<K, V> implements ICacheEntryFilter<K, V> {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean accept(K key, V value) {
    return true;
  }

  @Override
  public ICacheEntryFilter<K, V> coalesce(ICacheEntryFilter<K, V> other) {
    return this;
  }

  @Override
  public String toString() {
    return "AllCacheEntryFilter";
  }
}
