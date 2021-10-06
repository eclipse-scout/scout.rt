/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
 *          If instances within collection are {@link Comparable}, they must be mutually comparable (required for order
 *          normalization). E.g. do not use {@code DoCollection<Object>} and add {@link Integer} and {@link String}
 *          values).
 * @see DoEntity#doCollection(String) creator method
 */
@SuppressWarnings("squid:S2333") // redundant final
public final class DoCollection<V> extends AbstractDoCollection<V, Collection<V>> {

  public DoCollection() {
    this(null, null);
  }

  DoCollection(String attributeName, Consumer<DoNode<Collection<V>>> lazyCreate) {
    super(attributeName, lazyCreate, new ArrayList<>());
  }

  public static <V> DoCollection<V> of(Collection<V> collection) {
    DoCollection<V> doCollection = new DoCollection<>();
    doCollection.set(collection);
    return doCollection;
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
    super.set(newValue != null ? newValue : new ArrayList<>());
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
