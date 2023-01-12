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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract wrapper for a generic collection of values of type {@code V} inside a {@link DoEntity} object.
 */
@SuppressWarnings("squid:S2333") // redundant final
public abstract class AbstractDoCollection<V, COLLECTION extends Collection<V>> extends DoNode<COLLECTION> implements Iterable<V>, IDoCollection<V, COLLECTION> {

  AbstractDoCollection(String attributeName, Consumer<DoNode<COLLECTION>> lazyCreate, COLLECTION initialValue) {
    super(attributeName, lazyCreate, initialValue);
  }

  @Override
  public COLLECTION get() {
    create(); // collection needs to be marked as created on first access, since collection can be modified using getter
    return super.get();
  }

  @Override
  public boolean contains(V item) {
    if (!exists()) {
      return false;
    }
    return get().contains(item);
  }

  @Override
  public void add(V item) {
    get().add(item);
  }

  @Override
  public void addAll(Collection<? extends V> items) {
    if (items != null) {
      get().addAll(items);
    }
  }

  @Override
  @SafeVarargs
  public final void addAll(@SuppressWarnings("unchecked") V... items) {
    if (items != null) {
      addAll(Arrays.asList(items));
    }
  }

  @Override
  public boolean remove(V item) {
    if (!exists()) {
      return false;
    }
    return get().remove(item);
  }

  @Override
  public boolean removeAll(Collection<? extends V> items) {
    if (!exists()) {
      return false;
    }
    if (items != null) {
      return get().removeAll(items);
    }
    return false;
  }

  @Override
  @SafeVarargs
  public final boolean removeAll(@SuppressWarnings("unchecked") V... items) {
    if (!exists()) {
      return false;
    }
    if (items != null) {
      return removeAll(Arrays.asList(items));
    }
    return false;
  }

  @Override
  public void updateAll(Collection<? extends V> items) {
    clear();
    addAll(items);
  }

  @Override
  @SafeVarargs
  public final void updateAll(@SuppressWarnings("unchecked") V... items) {
    clear();
    addAll(items);
  }

  @Override
  public void clear() {
    if (!exists()) {
      return;
    }
    get().clear();
  }

  @Override
  public int size() {
    if (!exists()) {
      return 0;
    }
    return get().size();
  }

  @Override
  public boolean isEmpty() {
    if (!exists()) {
      return true;
    }
    return get().isEmpty();
  }

  @Override
  public Stream<V> stream() {
    if (!exists()) {
      return Stream.empty();
    }
    return get().stream();
  }

  @Override
  public Stream<V> parallelStream() {
    if (!exists()) {
      return Stream.empty();
    }
    return get().parallelStream();
  }

  @Override
  public Iterator<V> iterator() {
    if (!exists()) {
      return Collections.emptyIterator();
    }
    return get().iterator();
  }

  @Override
  public <VALUE> V findFirst(Function<V, DoValue<VALUE>> accessor, VALUE value) {
    return findFirst(DoPredicates.eq(accessor, value));
  }

  @Override
  public V findFirst(Predicate<V> predicate) {
    return stream()
        .filter(predicate)
        .findFirst()
        .orElse(null);
  }

  @Override
  public <VALUE> List<V> find(Function<V, DoValue<VALUE>> accessor, VALUE value) {
    return find(DoPredicates.eq(accessor, value));
  }

  @Override
  public List<V> find(Predicate<V> predicate) {
    return stream()
        .filter(predicate)
        .collect(Collectors.toList());
  }
}
