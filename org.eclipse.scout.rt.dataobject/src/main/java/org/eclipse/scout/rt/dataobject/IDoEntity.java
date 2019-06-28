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
package org.eclipse.scout.rt.dataobject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Base interface for all data object entities.
 *
 * @see {@link DoEntity} for a default base class implementation
 */
@Bean
public interface IDoEntity extends IDataObject {

  /**
   * @return Node of attribute {@code attributeName} or {@code null}, if attribute is not available.
   *         <p>
   *         The attribute node is either a {@link DoValue<T>} or a {@link DoList<T>} wrapper object.
   */
  DoNode<?> getNode(String attributeName);

  /**
   * @return the <i>read-only</i> map of all attributes as (key, value wrapped within DoNode)
   */
  Map<String, DoNode<?>> allNodes();

  /**
   * Adds new {@link DoValue} or {@link DoList} node to attributes map and assigns attribute name to specified
   * attribute.
   */
  default void putNode(String attributeName, DoNode<?> attribute) {
    Assertions.assertNotNull(attributeName, "attribute name cannot be null");
    Assertions.assertNotNull(attribute, "attribute node cannot be null for attribute name {}", attributeName);
    attribute.setAttributeName(attributeName);
  }

  /**
   * @return {@code true} if attribute with name {@code attributeName} exists (attribute value could be null), otherwise
   *         {@code false}
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
   * Removes {@link DoValue} or {@link DoList} attribute from attributes map.
   *
   * @return {@code true} if an element was removed
   */
  boolean remove(String attributeName);

  /**
   * Removes all {@link DoValue} or {@link DoList} attribute from attributes map that satisfy the given predicate.
   * Errors or runtime exceptions thrown during iteration or by the predicate are relayed to the caller.
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
   *         not available.
   *         <p>
   *         The attribute node is either a {@link DoValue<T>} or a {@link DoList<T>} wrapper object.
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
   *         not available.
   * @see IDoEntity#getNode(String) to get the wrapped {@link DoNode} attribute
   */
  default <T> T get(String attributeName, Class<T> type) {
    Assertions.assertNotNull(type, "provided type is null");
    return type.cast(get(attributeName));
  }

  /**
   * @return Value of attribute {@code attributeName} mapped to a custom type using specified {@code mapper} or
   *         {@code null} if attribute is not available.
   */
  default <T> T get(String attributeName, Function<Object, T> mapper) {
    Assertions.assertNotNull(mapper, "provided mapper function is null");
    return mapper.apply(get(attributeName));
  }

  /**
   * @return List value of attribute {@code attributeName} or {@code null} if attribute is not available.
   * @see IDoEntity#getNode(String) to get the wrapped attribute node
   */
  default List<Object> getList(String attributeName) {
    return getList(attributeName, Object.class);
  }

  /**
   * @return List value of attribute {@code attributeName} casted to specified {@code type} or {@code null} if attribute
   *         is not available.
   * @see IDoEntity#getNode(String) to get the wrapped attribute node
   */
  @SuppressWarnings("unchecked")
  default <T> List<T> getList(String attributeName, Class<T> type) {
    return optNode(attributeName)
        .map(n -> Assertions.assertType(n, DoList.class).get())
        .orElse(null);
  }

  /**
   * @return Value of list attribute {@code attributeName}, each element mapped to a custom type using specified
   *         {@code mapper} or {@code null} if attribute is not available.
   */
  default <T> List<T> getList(String attributeName, Function<Object, T> mapper) {
    Assertions.assertNotNull(mapper, "provided mapper function is null");
    return getList(attributeName).stream().map(item -> mapper.apply(item)).collect(Collectors.toList());
  }

  /**
   * @return Value of attribute {@code attributeName} converted to {@link BigDecimal} or {@code null} if attribute is
   *         not available.
   * @throws AssertionException
   *           if attribute value is not instance of {@link BigDecimal}
   */
  default BigDecimal getDecimal(String attributeName) {
    return TypeCastUtility.castValue(Assertions.assertType(get(attributeName), Number.class), BigDecimal.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} converted to a list of {@link BigDecimal} or {@code null} if
   *         attribute is not available.
   * @throws AssertionException
   *           if a list item value is not instance of {@link Number}
   */
  default List<BigDecimal> getDecimalList(String attributeName) {
    return getList(attributeName, item -> TypeCastUtility.castValue(Assertions.assertType(item, Number.class), BigDecimal.class));
  }

  /**
   * @return Value of attribute {@code attributeName} casted to {@link Boolean} or {@code null} if attribute is not
   *         available.
   * @throws AssertionException
   *           if attribute value is not instance of {@link Boolean}
   */
  default Boolean getBoolean(String attributeName) {
    return Assertions.assertType(get(attributeName), Boolean.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} casted to {@link List<Boolean>} or {@code null} if attribute
   *         is not available.
   * @throws AssertionException
   *           if a list item value is not instance of {@link Boolean}
   */
  default List<Boolean> getBooleanList(String attributeName) {
    return getList(attributeName, item -> Assertions.assertType(item, Boolean.class));
  }

  /**
   * @return Value of attribute {@code attributeName} casted to {@link String} or {@code null} if attribute is not
   *         available.
   * @throws AssertionException
   *           if attribute value is not instance of {@link String}
   */
  default String getString(String attributeName) {
    return Assertions.assertType(get(attributeName), String.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} casted to {@link String} or {@code null} if attribute is not
   *         available.
   * @throws AssertionException
   *           if a list item value is not instance of {@link String}
   */
  default List<String> getStringList(String attributeName) {
    return getList(attributeName, item -> Assertions.assertType(item, String.class));
  }

  /**
   * Removes {@link DoValue} or {@link DoList} attribute from attributes map.
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
   * Removes {@link DoValue} or {@link DoList} attribute node from attributes map.
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
}
