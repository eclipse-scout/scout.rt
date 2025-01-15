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
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Interface for a generic collection of values of type {@code V} inside a {@link DoEntity} object.
 */
public interface IDoCollection<V, COLLECTION extends Collection<V>> extends Iterable<V> {

  /**
   * @return {@code true} if this attribute is part of a {@link DoEntity}, otherwise {@code false}.
   */
  boolean exists();

  /**
   * @return modifiable collection of all items, never {@code null}.
   */
  COLLECTION get();

  /**
   * Returns <code>true</code> if this collection contains the item, <code>false</code> otherwise.
   */
  boolean contains(V item);

  /**
   * Adds the specified element to this collection.
   */
  void add(V item);

  /**
   * Adds all of the elements in the specified collection to this collection, in the order that they are returned by the
   * specified collection's iterator. Does not add any values if {@code items} is null.
   */
  void addAll(Collection<? extends V> items);

  /**
   * Adds all of the elements in the specified array to this collection, in the order that they are contained in the
   * array. Does not add any values if {@code items} is null.
   */
  @SuppressWarnings("unchecked")
  void addAll(V... items);

  /**
   * Removes the specified element from this collection, if it is present.
   *
   * @return {@code true} if this collection changed as a result of the call
   */
  boolean remove(V item);

  /**
   * Removes from this collection all of its elements that are contained in the specified collection. Does not remove
   * any values if {@code items} is null.
   *
   * @return {@code true} if this collection changed as a result of the call
   */
  boolean removeAll(Collection<? extends V> items);

  /**
   * Removes from this collection all of its elements that are contained in the specified array. Does not remove any
   * values if {@code items} is null.
   *
   * @return {@code true} if this collection changed as a result of the call
   */
  @SuppressWarnings("unchecked")
  boolean removeAll(V... items);

  /**
   * Replaces all items in this collection with the given collection of new {@code items}. If {@code items} is
   * {@code null}, the collection is cleared without adding any items.
   */
  void updateAll(Collection<? extends V> items);

  /**
   * Replaces all items in this collection with the given array of new {@code items}. If {@code items} is {@code null},
   * the collection is cleared without adding any items.
   */
  @SuppressWarnings("unchecked")
  void updateAll(V... items);

  /**
   * Removes all elements from this collection.
   */
  void clear();

  /**
   * @return the number of elements in this collection
   */
  int size();

  /**
   * @return {@code true} if this collection contains no elements, else {@code false}.
   */
  boolean isEmpty();

  /**
   * @return a sequential {@code Stream} with this collection as its source.
   */
  Stream<V> stream();

  /**
   * @return a possibly parallel {@code Stream} with this collection as its source.
   */
  Stream<V> parallelStream();

  @Override
  Iterator<V> iterator();

  /**
   * @return the first collection element which attribute given by the method reference is equal to the given value.
   * <code>null</code> is returned if there is no such collection element.
   */
  <VALUE> V findFirst(Function<V, DoValue<VALUE>> accessor, VALUE value);

  /**
   * @return the first collection element that evaluates to <code>true</code> when applied to the given
   * {@link Predicate}. <code>null</code> is returned if there is no such collection element.
   */
  V findFirst(Predicate<V> predicate);

  /**
   * @return all collection elements which attribute given by the method reference is equal to the given value. An empty
   * collection is returned if there are no such elements.
   */
  <VALUE> List<V> find(Function<V, DoValue<VALUE>> accessor, VALUE value);

  /**
   * @return all collection elements that are evaluating to <code>true</code> when applied to the given
   * {@link Predicate}. An empty collection is returned if there are no such elements.
   */
  List<V> find(Predicate<V> predicate);
}
