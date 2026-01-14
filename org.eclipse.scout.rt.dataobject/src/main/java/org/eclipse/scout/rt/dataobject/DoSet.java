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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Wrapper for a generic set of values of type {@code V} inside a {@link DoEntity} object.
 * {@link DataObjectHelper#normalize(IDataObject)} may be used to apply a deterministic order to {@link DoSet}.
 *
 * <p>Note: {@link DoSet} is backed by a {@link Set}, therefore great care must be exercised if mutable objects
 * are used as set elements. The behavior of a set is not specified if the value of an object is changed in
 * a manner that affects {@code equals} comparisons while the object is an element in the set. See javadoc
 * of {@link Set} for further details.<br>
 * {@link DoSet} guarantees correct equality {@link DoSet#equals(Object)}), hashcode ({@link DoSet#hashCode()})
 * and contains ({@link DoSet#contains(Object)}) operations if used as {@link DoSet} wrapper and not using the
 * wrapped {@link Set} instance directly.
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

  @Override
  public boolean contains(V item) {
    if (!exists()) {
      return false;
    }
    return get().stream().anyMatch(i -> ObjectUtility.equals(i, item));
  }

  // LinkedHashSet already implements hashCode without considering element position, thus no need to override valueHashCode.

  /**
   * Use CollectionUtility.equalsCollection which implements iterator-based equality, instead of {@link LinkedHashSet#equals(Object)}
   * which uses containsAll-based equality based on hashCode of contained objects which may be corrupted by mutating nested data objects.
   */
  @Override
  protected boolean valueEquals(DoNode other) {
    if (!exists() && !other.exists()) {
      return true;
    }
    //noinspection unchecked
    return CollectionUtility.equalsCollection(get(), (Collection<V>) other.get(), false);
  }

  @Override
  public String toString() {
    return "DoSet [m_set=" + (exists() ? get() : "[]") + " exists=" + exists() + "]";
  }
}
