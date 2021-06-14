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
    this(null, null);
  }

  DoList(String attributeName, Consumer<DoNode<List<V>>> lazyCreate) {
    super(attributeName, lazyCreate, new ArrayList<>());
  }

  public static <V> DoList<V> of(List<V> list) {
    DoList<V> doList = new DoList<>();
    doList.set(list);
    return doList;
  }

  /**
   * Replaces the internally wrapped list with the specified {@code newValue} list. If {@code newValue} is {@code null},
   * an empty list is used instead.
   * <p>
   * <b>Use a modifiable list implementation if the items should be modified using {@link DoList} methods.</b>
   */
  @Override
  public void set(List<V> newValue) {
    super.set(newValue != null ? newValue : new ArrayList<>());
  }

  /**
   * Returns the element at the specified position in this list.
   */
  public V get(int index) {
    return get().get(index);
  }

  /**
   * Removes the element at the specified position in this list.
   *
   * @return the element previously at the specified position
   */
  public V remove(int index) {
    return get().remove(index);
  }

  /**
   * Returns first element of this list or {@code null} if list is empty.
   */
  public V first() {
    return get().size() == 0 ? null : get(0);
  }

  /**
   * Returns last element of this list or {@code null} if list is empty.
   */
  public V last() {
    return get().size() == 0 ? null : get(get().size() - 1);
  }

  /**
   * @return an {@code ListIterator} over the elements in this list.
   */
  public ListIterator<V> listIterator() {
    return get().listIterator();
  }

  /**
   * Sorts the internal list using {@code comparator} and returns the list.
   */
  public List<V> sort(Comparator<V> comparator) {
    List<V> list = get();
    list.sort(comparator);
    return list;
  }

  // ArrayList already implemented hashCode/equals with considering element position, thus no need to override valueHashCode/valueEquals.

  @Override
  public String toString() {
    return "DoList [m_list=" + get() + " exists=" + exists() + "]";
  }
}
