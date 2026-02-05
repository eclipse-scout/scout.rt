/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.collection;

import java.util.TreeMap;
import java.util.function.Function;

/**
 * Map that normalizes keys before get and put. Useful if keys might be of different subclasses of K.
 *
 * @param <K>
 *     the type of keys maintained by this map
 * @param <V>
 *     the type of mapped values
 */
public class NormalizedKeyTreeMap<K, V> extends TreeMap<K, V> {
  private static final long serialVersionUID = 1L;

  private final Function<K, K> m_normalizeKeyFunction;

  public NormalizedKeyTreeMap(Function<K, K> normalizeKeyFunction) {
    m_normalizeKeyFunction = normalizeKeyFunction;
  }

  @Override
  public V get(Object key) {
    return super.get(normalize((K) key));
  }

  @Override
  public V put(K key, V value) {
    return super.put(normalize(key), value);
  }

  public K normalize(K key) {
    return m_normalizeKeyFunction.apply(key);
  }
}
