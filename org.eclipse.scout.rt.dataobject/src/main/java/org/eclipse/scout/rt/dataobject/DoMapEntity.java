/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Base type for data objects with {@link Map}-like structure using key type {@link String} and generic value type
 * {@code T}
 * <p>
 * Attribute definition methods for attributes of map value type {@code T} may be omitted, since all attributes without
 * a specific type definition using an accessor method are assumed to be of type {@code T}.
 * <p>
 * Example map data object using {@code AnotherDo} as map-value type and specifying an additional {@link Integer}
 * attribute {@code count}.
 *
 * <pre>
 * &#64;TypeName("ExampleEntity")
 * public class ExampleMapDo extends DoMap&lt;AnotherDo&gt; {
 *
 *   public DoValue&lt;Integer&gt; count() {
 *     return doValue("count");
 *   }
 *
 * }
 * </pre>
 *
 * @see DoEntity Examples for declaring typed attributes on {@link DoEntity}
 */
public class DoMapEntity<T> extends DoEntity {

  /**
   * @return A new empty {@link DoMapEntity}.
   */
  public static <T> DoMapEntity<T> create() {
    return of(null);
  }

  /**
   * Creates a new {@link DoMapEntity} holding all entires from the given source {@link Map}.
   *
   * @param source
   *     All entries of this {@link Map} will be added to the returned new {@link DoMapEntity}.
   * @return A new {@link DoMapEntity} holding all mappings from the given input {@link Map}.
   */
  @SuppressWarnings("unchecked")
  public static <T> DoMapEntity<T> of(Map<String, T> source) {
    DoMapEntity<T> doMapEntity = (DoMapEntity<T>) BEANS.get(DoMapEntity.class);
    return doMapEntity.putAll(source);
  }

  @Override
  public T get(String attributeName) {
    return mapValue(super.get(attributeName));
  }

  /**
   * Puts all entries of the given map into this {@link DoMapEntity}.
   *
   * @param sourceMap
   *     The source or {@code null}.
   * @return this DoMapEntity.
   */
  public DoMapEntity<T> putAll(Map<String, T> sourceMap) {
    if (sourceMap != null) {
      sourceMap.forEach(this::put);
    }
    return this;
  }

  @Override
  public Map<String, T> all() {
    return all(this::mapValue);
  }

  @SuppressWarnings("unchecked")
  protected T mapValue(Object value) {
    return (T) value;
  }
}
