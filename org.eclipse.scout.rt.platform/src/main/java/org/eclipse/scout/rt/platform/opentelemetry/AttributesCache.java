/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.opentelemetry.api.common.Attributes;

/**
 * 2-dimensional cache for the OpenTelemetry metric attributes without short living memory allocations on access.
 * <p>
 * This nested map cache helps to reduce short living memory allocations while handling metric value observations. If
 * possible the cache should be pre-populated/pre-allocated.
 * </p>
 * <p>
 * See also blog post
 * <a href="https://opentelemetry.io/blog/2024/java-metric-systems-compared/#opentelemetry-java-metrics">OpenTelemetry
 * Java Metrics Performance Comparison"</a>:
 *
 * <pre>
 * if all the distinct attribute sets are known ahead of time, they can and should be pre-allocated and held in a
 * constant Attributes variable, which reduces unnecessary memory allocations.
 * </pre>
 * </p>
 * <p>
 * <b>Important:</b> Do only use a cache for low cardinality attributes. Otherwise the memory usage of the cache will
 * cause more issues than it solves.
 * </p>
 *
 * @see Attributes
 */
public class AttributesCache<KEY1, KEY2> {

  public static <KEY1, KEY2> AttributesCache<KEY1, KEY2> of(int initialCapacityDimension1, int initialCapacityDimension2, BiFunction<KEY1, KEY2, Attributes> createAttributesFunction) {
    return new AttributesCache<>(initialCapacityDimension1, initialCapacityDimension2, createAttributesFunction);
  }

  private Map<KEY1, Map<KEY2, Attributes>> m_cache;
  private final Function<KEY1, Map<KEY2, Attributes>> m_newMapFunction;
  private final BiFunction<KEY1, KEY2, Attributes> m_createAttributesFunction;

  protected AttributesCache(int initialCapacityDimension1, int initialCapacityDimension2, BiFunction<KEY1, KEY2, Attributes> createAttributesFunction) {
    m_cache = new ConcurrentHashMap<>(initialCapacityDimension1);
    m_newMapFunction = k -> new ConcurrentHashMap<>(initialCapacityDimension2);
    m_createAttributesFunction = createAttributesFunction;
  }

  /**
   * Return cached {@link Attributes} instance, if absent, a new instance is created and added to the cache.
   * <p>
   * Attention: This method is not thread safe. The {@link Attributes} instance can be created multiple times in the
   * event of a cache miss by concurrent callers.
   * </p>
   */
  public Attributes getOrCreate(KEY1 key1, KEY2 key2) {
    Map<KEY2, Attributes> attributesMap = m_cache.computeIfAbsent(key1, m_newMapFunction);
    // primitive "computeIfAbsent" implementation to avoid memory allocation here (e.g. to create a mapping function for each call)
    Attributes attributes = attributesMap.get(key2);
    if (attributes == null) {
      attributes = m_createAttributesFunction.apply(key1, key2);
      attributesMap.put(key2, attributes);
    }
    return attributes;
  }

  /**
   * Use this method to pre-populate the cache for the given key1/2.
   */
  public void put(KEY1 key1, KEY2 key2) {
    m_cache.computeIfAbsent(key1, m_newMapFunction)
        .put(key2, m_createAttributesFunction.apply(key1, key2));
  }
}
