/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class contains several {@link Predicate}s that are useful for querying {@link DoNode} structures.
 */
public final class DoPredicates {

  private DoPredicates() {
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is equal to the given value.
   *
   * @param accessor
   *          method reference that resolves the {@link DoValue} of a {@link DoNode} (e.g. <code>MyEntity::id</code>).
   * @param value
   */
  public static <VALUE, DO_NODE> Predicate<DO_NODE> eq(Function<DO_NODE, DoValue<VALUE>> accessor, VALUE value) {
    assertNotNull(accessor, "accessor must not be null");
    return n -> Objects.equals(accessor.apply(n).get(), value);
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is not equal to the given value.
   *
   * @param accessor
   *          method reference that resolves the {@link DoValue} of a {@link DoNode} (e.g. <code>MyEntity::id</code>).
   * @param value
   */
  public static <VALUE, DO_NODE> Predicate<DO_NODE> ne(Function<DO_NODE, DoValue<VALUE>> accessor, VALUE value) {
    return eq(accessor, value).negate();
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is contained in the given collection of values.
   *
   * @param accessor
   *          method reference that resolves the {@link DoValue} of a {@link DoNode} (e.g. <code>MyEntity::id</code>).
   * @param value
   */
  public static <VALUE, DO_NODE> Predicate<DO_NODE> in(Function<DO_NODE, DoValue<VALUE>> accessor, Collection<VALUE> values) {
    assertNotNull(accessor, "accessor must not be null");
    assertNotNull(values, "values must not be null");
    return n -> values.contains(accessor.apply(n).get());
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is not contained in the given collection of values.
   *
   * @param accessor
   *          method reference that resolves the {@link DoValue} of a {@link DoNode} (e.g. <code>MyEntity::id</code>).
   * @param value
   */
  public static <VALUE, DO_NODE> Predicate<DO_NODE> notIn(Function<DO_NODE, DoValue<VALUE>> accessor, Collection<VALUE> values) {
    return in(accessor, values).negate();
  }

  /**
   * Predicate testing the existence of a {@link DoNode} within a {@link DoList} which satisfies the given predicate.
   *
   * @param accessor
   *          method reference that resolves the {@link DoList} of a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   * @param predicate
   */
  public static <DO_NODE, LIST_NODE> Predicate<DO_NODE> exists(Function<DO_NODE, DoList<LIST_NODE>> listAccessor, Predicate<LIST_NODE> predicate) {
    assertNotNull(listAccessor, "list accessor must not be null");
    assertNotNull(predicate, "predicate must not be null");
    return n -> listAccessor.apply(n).findFirst(predicate) != null;
  }

  /**
   * Predicate testing the absence of a {@link DoNode} within a {@link DoList} which satisfies the given predicate.
   *
   * @param accessor
   *          method reference that resolves the {@link DoList} of a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   * @param predicate
   */
  public static <DO_NODE, LIST_NODE> Predicate<DO_NODE> notExists(Function<DO_NODE, DoList<LIST_NODE>> listAccessor, Predicate<LIST_NODE> predicate) {
    return exists(listAccessor, predicate).negate();
  }

  /**
   * Predicate testing if the value of a particular {@link DoList} is empty.
   *
   * @param accessor
   *          method reference that resolves the {@link DoList} of a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   * @param predicate
   */
  public static <DO_NODE, LIST_NODE> Predicate<DO_NODE> empty(Function<DO_NODE, DoList<LIST_NODE>> listAccessor) {
    assertNotNull(listAccessor, "list accessor must not be null");
    return n -> listAccessor.apply(n).get().isEmpty();
  }

  /**
   * Predicate testing if the value of a particular {@link DoList} is not empty.
   *
   * @param accessor
   *          method reference that resolves the {@link DoList} of a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   * @param predicate
   */
  public static <DO_NODE, LIST_NODE> Predicate<DO_NODE> notEmpty(Function<DO_NODE, DoList<LIST_NODE>> listAccessor) {
    return empty(listAccessor).negate();
  }
}
