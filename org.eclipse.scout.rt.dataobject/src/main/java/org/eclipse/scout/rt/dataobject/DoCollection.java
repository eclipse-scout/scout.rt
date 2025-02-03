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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Wrapper for a generic collection of values of type {@code V} inside a {@link DoEntity} object. As opposed to
 * {@link DoList}, no order is guaranteed here and as opposed to {@link DoSet} it may contain duplicates.
 * {@link DataObjectHelper#normalize(IDataObject)} may be used to apply a deterministic order to {@link DoCollection}.
 *
 * @param <V>
 *     If instances within collection are {@link Comparable}, they must be mutually comparable (required for order
 *     normalization). E.g. do not use {@code DoCollection<Object>} and add {@link Integer} and {@link String}
 *     values).
 * @see DoEntity#doCollection(String) creator method
 */
@SuppressWarnings("squid:S2333") // redundant final
public final class DoCollection<V> extends AbstractDoCollection<V, Collection<V>> {

  public DoCollection() {
    this(null, null, null);
  }

  DoCollection(String attributeName, Consumer<DoNode<Collection<V>>> lazyCreate, Collection<V> initialValue) {
    super(attributeName, lazyCreate, emptyCollectionIfNull(initialValue));
  }

  public static <V> DoCollection<V> of(Collection<V> collection) {
    return new DoCollection<>(null, null, collection);
  }

  static <V> Collection<V> emptyCollectionIfNull(Collection<V> list) {
    return list != null ? list : new ArrayList<>();
  }

  /**
   * Replaces the internally wrapped collection with the specified {@code newValue} collection. If {@code newValue} is
   * {@code null}, an empty collection is used instead.
   * <p>
   * <b>Use a modifiable collection implementation if the items should be modified using {@link DoCollection}
   * methods.</b>
   */
  @Override
  public void set(Collection<V> newValue) {
    super.set(emptyCollectionIfNull(newValue));
  }

  @Override
  protected int valueHashCode() {
    if (!exists()) {
      return 0;
    }
    return CollectionUtility.hashCodeCollection(get(), false);
  }

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
    return "DoCollection [m_collection=" + (exists() ? get() : "[]") + " exists=" + exists() + "]";
  }
}
