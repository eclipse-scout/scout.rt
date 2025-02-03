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
   * Predicate testing if a particular {@link DoValue} is present.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> exists(Function<DO_ENTITY, DoValue<VALUE>> accessor) {
    assertNotNull(accessor, "accessor is required");
    return e -> accessor.apply(e).exists();
  }

  /**
   * Predicate testing if a particular {@link DoValue} is not present.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> notExists(Function<DO_ENTITY, DoValue<VALUE>> accessor) {
    return exists(accessor).negate();
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is null.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> isNull(Function<DO_ENTITY, DoValue<VALUE>> accessor) {
    assertNotNull(accessor, "accessor is required");
    return e -> accessor.apply(e).get() == null;
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is not null.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> isNotNull(Function<DO_ENTITY, DoValue<VALUE>> accessor) {
    return isNull(accessor).negate();
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is equal to the given value.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> eq(Function<DO_ENTITY, DoValue<VALUE>> accessor, VALUE value) {
    assertNotNull(accessor, "accessor must not be null");
    return e -> Objects.equals(accessor.apply(e).get(), value);
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is not equal to the given value.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> ne(Function<DO_ENTITY, DoValue<VALUE>> accessor, VALUE value) {
    return eq(accessor, value).negate();
  }

  /**
   * Predicate testing if the {@link Comparable} value of a particular {@link DoValue} is less than or equals to the
   * given value. The predicate returns {@code null} if, the {@link DoValue} contains {@code null}.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   * @param value
   *     a non-null value that the resolved {@link DoValue} will be compared to
   */
  public static <VALUE extends Comparable<VALUE>, DO_ENTITY> Predicate<DO_ENTITY> le(Function<DO_ENTITY, DoValue<VALUE>> accessor, VALUE value) {
    assertNotNull(accessor, "accessor is required");
    assertNotNull(value, "value is required");
    return e -> {
      final DoValue<VALUE> doValue = accessor.apply(e);
      return doValue.get() != null && doValue.get().compareTo(value) <= 0;
    };
  }

  /**
   * Predicate testing if the {@link Comparable} value of a particular {@link DoValue} is less than the given value. The
   * predicate returns {@code null} if, the {@link DoValue} contains {@code null}.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   * @param value
   *     a non-null value that the resolved {@link DoValue} will be compared to
   */
  public static <VALUE extends Comparable<VALUE>, DO_ENTITY> Predicate<DO_ENTITY> lt(Function<DO_ENTITY, DoValue<VALUE>> accessor, VALUE value) {
    assertNotNull(accessor, "accessor is required");
    assertNotNull(value, "value is required");
    return e -> {
      final DoValue<VALUE> doValue = accessor.apply(e);
      return doValue.get() != null && doValue.get().compareTo(value) < 0;
    };
  }

  /**
   * Predicate testing if the {@link Comparable} value of a particular {@link DoValue} is greater than or equals to the
   * given value. The predicate returns {@code null} if, the {@link DoValue} contains {@code null}.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   * @param value
   *     a non-null value that the resolved {@link DoValue} will be compared to
   */
  public static <VALUE extends Comparable<VALUE>, DO_ENTITY> Predicate<DO_ENTITY> ge(Function<DO_ENTITY, DoValue<VALUE>> accessor, VALUE value) {
    assertNotNull(accessor, "accessor is required");
    assertNotNull(value, "value is required");
    return e -> {
      final DoValue<VALUE> doValue = accessor.apply(e);
      return doValue.get() != null && doValue.get().compareTo(value) >= 0;
    };
  }

  /**
   * Predicate testing if the {@link Comparable} value of a particular {@link DoValue} is greater than the given value.
   * The predicate returns {@code null} if, the {@link DoValue} contains {@code null}.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   * @param value
   *     a non-null value that the resolved {@link DoValue} will be compared to
   */
  public static <VALUE extends Comparable<VALUE>, DO_ENTITY> Predicate<DO_ENTITY> gt(Function<DO_ENTITY, DoValue<VALUE>> accessor, VALUE value) {
    assertNotNull(accessor, "accessor is required");
    assertNotNull(value, "value is required");
    return e -> {
      final DoValue<VALUE> doValue = accessor.apply(e);
      return doValue.get() != null && doValue.get().compareTo(value) > 0;
    };
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is contained in the given collection of values.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> in(Function<DO_ENTITY, DoValue<VALUE>> accessor, Collection<VALUE> values) {
    assertNotNull(accessor, "accessor must not be null");
    assertNotNull(values, "values must not be null");
    return e -> values.contains(accessor.apply(e).get());
  }

  /**
   * Predicate testing if the value of a particular {@link DoValue} is not contained in the given collection of values.
   *
   * @param accessor
   *     method reference that resolves the {@link DoValue} of a {@link DoEntity} (e.g. <code>MyEntity::id</code>).
   */
  public static <VALUE, DO_ENTITY> Predicate<DO_ENTITY> notIn(Function<DO_ENTITY, DoValue<VALUE>> accessor, Collection<VALUE> values) {
    return in(accessor, values).negate();
  }

  /**
   * Predicate testing the existence of a value within a DO collection ({@link DoList}, {@link DoSet},
   * {@link DoCollection}) which satisfies the given predicate.
   *
   * @param collectionAccessor
   *     method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *     a {@link DoEntity} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_ENTITY, COLLECTION_ITEM> Predicate<DO_ENTITY> exists(Function<DO_ENTITY, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor, Predicate<COLLECTION_ITEM> predicate) {
    assertNotNull(collectionAccessor, "collection accessor must not be null");
    assertNotNull(predicate, "predicate must not be null");
    return n -> collectionAccessor.apply(n).findFirst(predicate) != null;
  }

  /**
   * Predicate testing the absence of a value within a DO collection ({@link DoList}, {@link DoSet},
   * {@link DoCollection}) which satisfies the given predicate.
   *
   * @param collectionAccessor
   *     method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *     a {@link DoEntity} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_ENTITY, COLLECTION_ITEM> Predicate<DO_ENTITY> notExists(Function<DO_ENTITY, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor, Predicate<COLLECTION_ITEM> predicate) {
    return exists(collectionAccessor, predicate).negate();
  }

  /**
   * Predicate testing if the value of a particular DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection})
   * is empty.
   *
   * @param collectionAccessor
   *     method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *     a {@link DoEntity} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_ENTITY, COLLECTION_ITEM> Predicate<DO_ENTITY> empty(Function<DO_ENTITY, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor) {
    assertNotNull(collectionAccessor, "collection accessor must not be null");
    return n -> collectionAccessor.apply(n).get().isEmpty();
  }

  /**
   * Predicate testing if the value of a particular DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection})
   * is not empty.
   *
   * @param collectionAccessor
   *     method reference that resolves the DO collection ({@link DoList}, {@link DoSet}, {@link DoCollection}) of
   *     a {@link DoEntity} (e.g. <code>MyEntity::items</code>).
   */
  public static <DO_ENTITY, COLLECTION_ITEM> Predicate<DO_ENTITY> notEmpty(Function<DO_ENTITY, IDoCollection<COLLECTION_ITEM, ?>> collectionAccessor) {
    return empty(collectionAccessor).negate();
  }
}
