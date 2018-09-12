/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper for a generic list of values of type {@code V} inside a {@link DoEntity} object.
 *
 * @see DoEntity#doList(String) creator method
 */
public final class DoList<V> extends DoNode<List<V>> implements IDataObject, Iterable<V> {

  public DoList() {
    this(null, null);
  }

  protected DoList(String attributeName, Consumer<DoNode<List<V>>> lazyCreate) {
    super(attributeName, lazyCreate, new ArrayList<>());
  }

  public static <V> DoList<V> of(List<V> list) {
    DoList<V> doList = new DoList<>();
    doList.set(list);
    return doList;
  }

  /**
   * @return modifiable list of all items, never {@code null}.
   */
  @Override
  public List<V> get() {
    create(); // DoList needs to be marked as created on first access, since list can be modified using getter
    return super.get();
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
   * Appends the specified element to the end of this list.
   */
  public void add(V item) {
    get().add(item);
  }

  /**
   * Appends all of the elements in the specified collection to the end of this list, in the order that they are
   * returned by the specified collection's iterator. Does not append any values if {@code items} is null.
   */
  public void addAll(Collection<? extends V> items) {
    if (items != null) {
      get().addAll(items);
    }
  }

  /**
   * Appends all of the elements in the specified array to the end of this list, in the order that they are contained in
   * the array. Does not append any values if {@code items} is null.
   */
  public void addAll(@SuppressWarnings("unchecked") V... items) {
    if (items != null) {
      addAll(Arrays.asList(items));
    }
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
   * Removes the first occurrence of the specified element from this list, if it is present.
   *
   * @return {@code true} if this list changed as a result of the call
   */
  public boolean remove(V item) {
    return get().remove(item);
  }

  /**
   * Removes from this list all of its elements that are contained in the specified collection. Does not remove any
   * values if {@code items} is null.
   *
   * @return {@code true} if this list changed as a result of the call
   */
  public boolean removeAll(Collection<? extends V> items) {
    if (items != null) {
      return get().removeAll(items);
    }
    return false;
  }

  /**
   * Removes from this list all of its elements that are contained in the specified array. Does not remove any values if
   * {@code items} is null.
   *
   * @return {@code true} if this list changed as a result of the call
   */
  public boolean removeAll(@SuppressWarnings("unchecked") V... items) {
    if (items != null) {
      return removeAll(Arrays.asList(items));
    }
    return false;
  }

  /**
   * Replaces all items in this list with the given collection of new {@code items}. If {@code items} is {@code null},
   * the list is cleared without adding any items.
   */
  public void updateAll(Collection<? extends V> items) {
    clear();
    addAll(items);
  }

  /**
   * Replaces all items in this list with the given array of new {@code items}. If {@code items} is {@code null}, the
   * list is cleared without adding any items.
   */
  public void updateAll(@SuppressWarnings("unchecked") V... items) {
    clear();
    addAll(items);
  }

  /**
   * Removes all elements from this list.
   */
  public void clear() {
    if (get().isEmpty()) {
      return;
    }
    get().clear();
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
   * @return the number of elements in this list
   */
  public int size() {
    return get().size();
  }

  /**
   * @return {@code true} if this list contains no elements, else {@code false}.
   */
  public boolean isEmpty() {
    return get().isEmpty();
  }

  /**
   * @returns a sequential {@code Stream} with this list as its source.
   */
  public Stream<V> stream() {
    return get().stream();
  }

  /**
   * @returns a possibly parallel {@code Stream} with this list as its source.
   */
  public Stream<V> parallelStream() {
    return get().parallelStream();
  }

  @Override
  public Iterator<V> iterator() {
    return get().iterator();
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
    Collections.sort(get(), comparator);
    return get();
  }

  /**
   * @return the first list element which attribute given by the method reference is equal to the given value.
   *         <code>null</code> is returned if there is no such list element.
   */
  public <VALUE> V findFirst(Function<V, DoValue<VALUE>> accessor, VALUE value) {
    return findFirst(DoPredicates.eq(accessor, value));
  }

  /**
   * @return the first list element that evaluates to <code>true</code> when applied to the given {@link Predicate}.
   *         <code>null</code> is returned if there is no such list element.
   */
  public V findFirst(Predicate<V> predicate) {
    return stream()
        .filter(predicate)
        .findFirst()
        .orElse(null);
  }

  /**
   * @return all list elements which attribute given by the method reference is equal to the given value. An empty list
   *         is returned if there are no such elements.
   */
  public <VALUE> List<V> find(Function<V, DoValue<VALUE>> accessor, VALUE value) {
    return find(DoPredicates.eq(accessor, value));
  }

  /**
   * @return all list elements that are evaluating to <code>true</code> when applied to the given {@link Predicate}. An
   *         empty list is returned if there are no such elements.
   */
  public List<V> find(Predicate<V> predicate) {
    return stream()
        .filter(predicate)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "DoList [m_list=" + get() + " exists=" + exists() + "]";
  }
}
