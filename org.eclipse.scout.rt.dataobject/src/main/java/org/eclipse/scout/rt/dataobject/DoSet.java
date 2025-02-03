/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Wrapper for a generic set of values of type {@code V} inside a {@link DoEntity} object.
 * {@link DataObjectHelper#normalize(IDataObject)} may be used to apply a deterministic order to {@link DoSet}.
 *
 * @param <V>
 *     If instances within set are {@link Comparable}, they must be mutually comparable (required for order
 *     normalization). E.g. do not use {@code DoSet<Object>} and add {@link Integer} and {@link Long} values).
 * @see DoEntity#doSet(String) creator method
 */
@SuppressWarnings("squid:S2333") // redundant final
public final class DoSet<V> extends AbstractDoCollection<V, Set<V>> {

  public DoSet() {
    this(null, null, null);
  }

  DoSet(String attributeName, Consumer<DoNode<Set<V>>> lazyCreate, Set<V> initialValue) {
    // Even if the order within a set is not relevant, using a LinkedHashSet here to have a deterministic behavior by default.
    super(attributeName, lazyCreate, emptySetIfNull(initialValue));
  }

  public static <V> DoSet<V> of(Set<V> set) {
    return new DoSet<>(null, null, set);
  }

  static <V> Set<V> emptySetIfNull(Set<V> set) {
    return set != null ? set : new LinkedHashSet<>();
  }

  /**
   * Replaces the internally wrapped set with the specified {@code newValue} set. If {@code newValue} is {@code null},
   * an empty set is used instead.
   * <p>
   * <b>Use a modifiable set implementation if the items should be modified using {@link DoSet} methods.</b>
   */
  @Override
  public void set(Set<V> newValue) {
    super.set(emptySetIfNull(newValue));
  }

  // LinkedHashSet already implemented hashCode/equals without considering element position, thus no need to override valueHashCode/valueEquals.

  @Override
  public String toString() {
    return "DoSet [m_set=" + (exists() ? get() : "[]") + " exists=" + exists() + "]";
  }
}
