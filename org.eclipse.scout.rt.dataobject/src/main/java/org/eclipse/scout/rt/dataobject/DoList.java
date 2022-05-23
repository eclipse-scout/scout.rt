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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * Wrapper for a generic list of values of type {@code V} inside a {@link DoEntity} object.
 *
 * @see DoEntity#doList(String) creator method
 */
@SuppressWarnings("squid:S2333") // redundant final
public final class DoList<V> extends AbstractDoCollection<V, List<V>> implements IDataObject {

  public DoList() {
    this(null, null, null);
  }

  DoList(String attributeName, Consumer<DoNode<List<V>>> lazyCreate, List<V> initialValue) {
    super(attributeName, lazyCreate, emptyListIfNull(initialValue));
  }

  public static <V> DoList<V> of(List<V> list) {
    return new DoList<>(null, null, list);
  }

  static <V> List<V> emptyListIfNull(List<V> list) {
    return list != null ? list : new ArrayList<>();
  }

  /**
   * Replaces the internally wrapped list with the specified {@code newValue} list. If {@code newValue} is {@code null},
   * an empty list is used instead.
   * <p>
   * <b>Use a modifiable list implementation if the items should be modified using {@link DoList} methods.</b>
   */
  @Override
  public void set(List<V> newValue) {
    super.set(emptyListIfNull(newValue));
  }

  /**
   * Returns the element at the specified position in this list.
   */
  public V get(int index) {
    if (!exists()) {
      throw new IndexOutOfBoundsException("Node doesn't exist");
    }
    return get().get(index);
  }

  /**
   * Removes the element at the specified position in this list.
   *
   * @return the element previously at the specified position
   */
  public V remove(int index) {
    if (!exists()) {
      throw new IndexOutOfBoundsException("Node doesn't exist");
    }
    return get().remove(index);
  }

  /**
   * Returns first element of this list or {@code null} if list is empty.
   */
  public V first() {
    return size() == 0 ? null : get(0);
  }

  /**
   * Returns last element of this list or {@code null} if list is empty.
   */
  public V last() {
    return size() == 0 ? null : get(get().size() - 1);
  }

  /**
   * @return an {@code ListIterator} over the elements in this list.
   */
  public ListIterator<V> listIterator() {
    if (!exists()) {
      return Collections.emptyListIterator();
    }
    return get().listIterator();
  }

  /**
   * Sorts the internal list using {@code comparator}.
   */
  public void sort(Comparator<V> comparator) {
    if (!exists()) {
      return;
    }
    List<V> list = get();
    list.sort(comparator);
  }

  // ArrayList already implemented hashCode/equals with considering element position, thus no need to override valueHashCode/valueEquals.

  @Override
  public String toString() {
    return "DoList [m_list=" + (exists() ? get() : "[]") + " exists=" + exists() + "]";
  }
}
