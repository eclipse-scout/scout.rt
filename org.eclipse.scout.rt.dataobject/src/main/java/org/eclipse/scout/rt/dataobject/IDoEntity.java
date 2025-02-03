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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Base interface for all data object entities.
 *
 * @see DoEntity for a default base class implementation
 */
@Bean
public interface IDoEntity extends IDataObject {

  /**
   * @return Node of attribute {@code attributeName} or {@code null}, if attribute is not available.
   * <p>
   * The attribute node is either a {@link DoValue}, {@link DoList}, {@link DoSet} or a {@link DoCollection}
   * wrapper object.
   */
  DoNode<?> getNode(String attributeName);

  /**
   * @return the <i>read-only</i> map of all attributes as (key, value wrapped within DoNode)
   */
  Map<String, DoNode<?>> allNodes();

  /**
   * Adds new {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} node to attributes map and assigns
   * attribute name to specified attribute.
   */
  default void putNode(String attributeName, DoNode<?> attribute) {
    assertNotNull(attributeName, "attribute name cannot be null");
    assertNotNull(attribute, "attribute node cannot be null for attribute name {}", attributeName);
    attribute.setAttributeName(attributeName);
  }

  /**
   * Adds new value to attribute map of entity if the value satisfies the given {@code predicate}.
   */
  default void putIf(String attributeName, Object value, Predicate<? super Object> predicate) {
    if (predicate.test(value)) {
      put(attributeName, value);
    }
  }

  /**
   * Adds new list value to attribute map of entity if the value satisfies the given {@code predicate}.
   */
  default <V> void putListIf(String attributeName, List<V> value, Predicate<? super List<V>> predicate) {
    if (predicate.test(value)) {
      putList(attributeName, value);
    }
  }

  /**
   * Adds new set value to attribute map of entity if the value satisfies the given {@code predicate}.
   */
  default <V> void putSetIf(String attributeName, Set<V> value, Predicate<? super Set<V>> predicate) {
    if (predicate.test(value)) {
      putSet(attributeName, value);
    }
  }

  /**
   * Adds new collection value to attribute map of entity if the value satisfies the given {@code predicate}.
   */
  default <V> void putCollectionIf(String attributeName, Collection<V> value, Predicate<? super Collection<V>> predicate) {
    if (predicate.test(value)) {
      putCollection(attributeName, value);
    }
  }

  /**
   * @return {@code true} if attribute with name {@code attributeName} exists (attribute value could be null), otherwise
   * {@code false}
   */
  boolean has(String attributeName);

  /**
   * Adds new value to attribute map. The value is wrapped within a {@link DoValue}.
   */
  void put(String attributeName, Object value);

  /**
   * Adds new list value to attribute map. The value is wrapped within a {@link DoList}.
   */
  <V> void putList(String attributeName, List<V> value);

  /**
   * Adds new set value to attribute map. The value is wrapped within a {@link DoSet}.
   */
  <V> void putSet(String attributeName, Set<V> value);

  /**
   * Adds new collection value to attribute map. The value is wrapped within a {@link DoCollection}.
   */
  <V> void putCollection(String attributeName, Collection<V> value);

  /**
   * Removes {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} attribute from attributes map.
   *
   * @return {@code true} if an element was removed
   */
  boolean remove(String attributeName);

  /**
   * Removes all {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} attribute from attributes map
   * that satisfy the given predicate. Errors or runtime exceptions thrown during iteration or by the predicate are
   * relayed to the caller.
   *
   * @return {@code true} if any element were removed
   */
  boolean removeIf(Predicate<? super DoNode<?>> filter);

  /**
   * @return the map of all attributes as (key, unwrapped value)
   */
  Map<String, ?> all();

  // ----- convenience accessor methods ----- //

  /**
   * @return {@link Optional} of node with attribute {@code attributeName} or empty {@link Optional}, if attribute is
   * not available.
   * <p>
   * The attribute node is either a {@link DoValue}, {@link DoList}, {@link DoSet} or a {@link DoCollection}
   * wrapper object.
   */
  default Optional<DoNode<?>> optNode(String attributeName) {
    return Optional.ofNullable(getNode(attributeName));
  }

  /**
   * @return Value of attribute {@code attributeName} or {@code null}, if attribute is not available.
   * @see IDoEntity#getNode(String) to get the wrapped {@link DoNode} attribute
   */
  default Object get(String attributeName) {
    DoNode<?> node = getNode(attributeName);
    if (node != null) {
      return node.get();
    }
    return null;
  }

  /**
   * @return Value of attribute {@code attributeName} casted to specified {@code type} or {@code null} if attribute is
   * not available.
   * @see IDoEntity#getNode(String) to get the wrapped {@link DoNode} attribute
   */
  default <T> T get(String attributeName, Class<T> type) {
    assertNotNull(type, "provided type is null");
    return type.cast(get(attributeName));
  }

  /**
   * @return Value of attribute {@code attributeName} mapped to a custom type using specified {@code mapper} or
   * {@code null} if attribute is not available.
   */
  default <T> T get(String attributeName, Function<Object, T> mapper) {
    assertNotNull(mapper, "provided mapper function is null");
    return mapper.apply(get(attributeName));
  }

  /**
   * @return List value of attribute {@code attributeName}. If the attribute is not available, an empty list is added as
   * attribute value into this entity and the list is returned.
   * @see IDoEntity#getNode(String) to get the wrapped attribute node
   * @see IDoEntity#optList(String, Class) to get a list attribute without adding the attribute into this entity if it
   * is not available
   */
  default List<Object> getList(String attributeName) {
    return getList(attributeName, Object.class);
  }

  /**
   * @return List value of attribute {@code attributeName} casted to specified {@code type}. If the attribute is not
   * available, an empty list is added as attribute value into this entity and the list is returned.
   * @see IDoEntity#getNode(String) to get the wrapped attribute node
   * @see IDoEntity#optList(String, Class) to get a list attribute without adding the attribute into this entity if it
   * is not available
   */
  @SuppressWarnings("unchecked")
  default <T> List<T> getList(String attributeName, Class<T> type) {
    if (!has(attributeName)) {
      // create the attribute with default value (empty list) if not available
      putList(attributeName, null);
    }
    return ((DoList<T>) Assertions.assertType(getNode(attributeName), DoList.class)).get();
  }

  /**
   * @return Value of list attribute {@code attributeName}, each element mapped to a custom type using specified
   * {@code mapper}. If the attribute is not available, an empty list is added as attribute value into this
   * entity and the list is returned.
   * @see IDoEntity#optList(String, Class) to get a list attribute without adding the attribute into this entity if it
   * is not available
   */
  default <T> List<T> getList(String attributeName, Function<Object, T> mapper) {
    assertNotNull(mapper, "provided mapper function is null");
    return getList(attributeName).stream().map(mapper).collect(Collectors.toList());
  }

  /**
   * @return Optional list value of attribute {@code attributeName}. If the attribute is not available, an empty
   * optional is returned.
   * @see IDoEntity#getNode(String) to get the wrapped attribute node
   */
  default Optional<List<Object>> optList(String attributeName) {
    return optList(attributeName, Object.class);
  }

  /**
   * @return Optional list value of attribute {@code attributeName} casted to specified {@code type}. If the attribute
   * is not available, an empty optional is returned.
   * @see IDoEntity#getNode(String) to get the wrapped attribute node
   */
  @SuppressWarnings("unchecked")
  default <T> Optional<List<T>> optList(String attributeName, Class<T> type) {
    return optNode(attributeName)
        .map(n -> ((DoList<T>) Assertions.assertType(n, DoList.class)).get());
  }

  /**
   * @return Value of attribute {@code attributeName} converted to {@link BigDecimal} or {@code null} if attribute is
   * not available.
   * @throws AssertionException
   *     if attribute value is not instance of {@link BigDecimal}
   */
  default BigDecimal getDecimal(String attributeName) {
    return TypeCastUtility.castValue(Assertions.assertType(get(attributeName), Number.class), BigDecimal.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} converted to a list of {@link BigDecimal}. If the attribute
   * is not available, an empty list is added as attribute value into this entity and the list is returned.
   * @throws AssertionException
   *     if a list item value is not instance of {@link Number}
   */
  default List<BigDecimal> getDecimalList(String attributeName) {
    return getList(attributeName, item -> TypeCastUtility.castValue(Assertions.assertType(item, Number.class), BigDecimal.class));
  }

  /**
   * @return Value of attribute {@code attributeName} casted to {@link Boolean} or {@code null} if attribute is not
   * available.
   * @throws AssertionException
   *     if attribute value is not instance of {@link Boolean}
   */
  default Boolean getBoolean(String attributeName) {
    return Assertions.assertType(get(attributeName), Boolean.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} casted to {@link List<Boolean>}. If the attribute is not
   * available, an empty list is added as attribute value into this entity and the list is returned.
   * @throws AssertionException
   *     if a list item value is not instance of {@link Boolean}
   */
  default List<Boolean> getBooleanList(String attributeName) {
    return getList(attributeName, item -> Assertions.assertType(item, Boolean.class));
  }

  /**
   * @return Value of attribute {@code attributeName} casted to {@link String} or {@code null} if attribute is not
   * available.
   * @throws AssertionException
   *     if attribute value is not instance of {@link String}
   */
  default String getString(String attributeName) {
    return Assertions.assertType(get(attributeName), String.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} casted to {@link String}. If the attribute is not available,
   * an empty list is added as attribute value into this entity and the list is returned.
   * @throws AssertionException
   *     if a list item value is not instance of {@link String}
   */
  default List<String> getStringList(String attributeName) {
    return getList(attributeName, item -> Assertions.assertType(item, String.class));
  }

  /**
   * Removes {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} attribute from attributes map.
   * <p>
   * Example:
   *
   * <pre>
   * dataObject.remove(dataObject::attributeName);
   * </pre>
   *
   * @return {@code true} if an element was removed
   */
  default boolean remove(Supplier<? extends DoNode<?>> nodeAccessor) {
    return remove(nodeAccessor.get());
  }

  /**
   * Removes {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} attribute node from attributes map.
   * <p>
   * Example:
   *
   * <pre>
   * dataObject.remove(dataObject:attributeName());
   * </pre>
   *
   * @return {@code true} if an element was removed
   */
  default boolean remove(DoNode<?> node) {
    return remove(node.getAttributeName());
  }

  /**
   * Returns {@code true} if this entity contains no attributes.
   *
   * @return {@code true} if this entity contains no attributes.
   */
  default boolean isEmpty() {
    return allNodes().isEmpty();
  }

  /**
   * @return <code>true</code> if contributions are available, <code>false</code> otherwise.
   */
  boolean hasContributions();

  /**
   * For read-only calls it's recommended to call {@link #hasContributions()} before calling this method because this
   * method might create an internal representation to store contributions which is usually not necessary if used
   * read-only.
   *
   * @return An mutable collection of DO entity contributions (never <code>null</code>).
   */
  Collection<IDoEntityContribution> getContributions();

  /**
   * @return Existing DO entity contribution for this contribution class if available, otherwise creates a new DO entity
   * contribution instance, adds it to the contributions and returns it.
   */
  default <CONTRIBUTION extends IDoEntityContribution> CONTRIBUTION contribution(Class<CONTRIBUTION> contributionClass) {
    if (!hasContribution(contributionClass)) {
      CONTRIBUTION contribution = BEANS.get(contributionClass);
      putContribution(contribution);
      return contribution;
    }

    return getContribution(contributionClass);
  }

  /**
   * @return DO entity contribution for this contribution class if available, <code>null</code> otherwise.
   */
  default <CONTRIBUTION extends IDoEntityContribution> CONTRIBUTION getContribution(Class<CONTRIBUTION> contributionClass) {
    assertNotNull(contributionClass, "contributionClass is required");
    if (!hasContributions()) {
      return null;
    }
    return getContributions().stream()
        .filter(contribution -> contributionClass.equals(contribution.getClass()))
        .findFirst()
        .map(contributionClass::cast)
        .orElse(null);
  }

  /**
   * @return <code>true</code> if the DO entity contribution for this contribution class is available,
   * <code>false</code> otherwise.
   */
  default boolean hasContribution(Class<? extends IDoEntityContribution> contributionClass) {
    return getContribution(contributionClass) != null;
  }

  /**
   * Adds a new DO entity contribution. An existing contribution for the same contribution class is overridden.
   *
   * @param contribution
   *     Contribution to add.
   */
  default void putContribution(IDoEntityContribution contribution) {
    assertNotNull(contribution, "contribution is required");
    removeContribution(contribution.getClass());
    getContributions().add(contribution);
  }

  /**
   * @return <code>true</code> if the DO entity contribution was available and removed, <code>false</code> otherwise.
   */
  default boolean removeContribution(Class<? extends IDoEntityContribution> contributionClass) {
    if (!hasContributions()) {
      return false;
    }
    return getContributions().removeIf(contribution -> contributionClass.equals(contribution.getClass()));
  }
}
