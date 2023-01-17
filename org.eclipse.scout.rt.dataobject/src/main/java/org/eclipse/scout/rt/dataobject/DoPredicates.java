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
   */
  public static <VALUE, DO_NODE> Predicate<DO_NODE> ne(Function<DO_NODE, DoValue<VALUE>> accessor, VALUE value) {
    return eq(accessor, value).negate();
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is contained in the given collection of values.
   *
   * @param accessor
   *          method reference that resolves the {@link DoValue} of a {@link DoNode} (e.g. <code>MyEntity::id</code>).
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
   */
  public static <VALUE, DO_NODE> Predicate<DO_NODE> notIn(Function<DO_NODE, DoValue<VALUE>> accessor, Collection<VALUE> values) {
    return in(accessor, values).negate();
  }

  /**
   * Predicate testing the existence of a {@link DoNode} within a DO collection ({@link DoList}, {@link DoSet},
   * {@link DoCollection}) which satisfies the given predicate.
   *
   * @param collectionAccessor
   *          method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *          a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_NODE, COLLECTION_ITEM> Predicate<DO_NODE> exists(Function<DO_NODE, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor, Predicate<COLLECTION_ITEM> predicate) {
    assertNotNull(collectionAccessor, "collection accessor must not be null");
    assertNotNull(predicate, "predicate must not be null");
    return n -> collectionAccessor.apply(n).findFirst(predicate) != null;
  }

  /**
   * Predicate testing the absence of a {@link DoNode} within a DO collection ({@link DoList}, {@link DoSet},
   * {@link DoCollection}) which satisfies the given predicate.
   *
   * @param collectionAccessor
   *          method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *          a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_NODE, COLLECTION_ITEM> Predicate<DO_NODE> notExists(Function<DO_NODE, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor, Predicate<COLLECTION_ITEM> predicate) {
    return exists(collectionAccessor, predicate).negate();
  }

  /**
   * Predicate testing if the value of a particular DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection})
   * is empty.
   *
   * @param collectionAccessor
   *          method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *          a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_NODE, COLLECTION_ITEM> Predicate<DO_NODE> empty(Function<DO_NODE, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor) {
    assertNotNull(collectionAccessor, "collection accessor must not be null");
    return n -> collectionAccessor.apply(n).get().isEmpty();
  }

  /**
   * Predicate testing if the value of a particular DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection})
   * is not empty.
   *
   * @param collectionAccessor
   *          method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *          a {@link DoNode} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_NODE, COLLECTION_ITEM> Predicate<DO_NODE> notEmpty(Function<DO_NODE, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor) {
    return empty(collectionAccessor).negate();
  }
}
