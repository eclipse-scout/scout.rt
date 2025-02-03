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

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StreamUtility;

/**
 * Base type for all data object beans. Attributes of a data object bean are defined using {@link #doValue(String)} and
 * {@link #doList(String)} methods.
 * <p>
 * Example entity with a value and a list attribute:
 *
 * <pre>
 * &#64;TypeName("ExampleEntity")
 * &#64;TypeVersion("scout-8.0.0")
 * public class ExampleEntityDo extends DoEntity {
 *
 *   public DoValue&lt;Date&gt; createdOn() {
 *     return doValue("createdOn");
 *   }
 *
 *   public DoList&lt;String&gt; nodes() {
 *     return doList("nodes");
 *   }
 * }
 * </pre>
 */
public class DoEntity implements IDoEntity {

  private final Map<String, DoNode<?>> m_attributes = new LinkedHashMap<>();

  private List<IDoEntityContribution> m_contributions; // lazy init, because contributions are used rarely

  /**
   * @return Node of attribute {@code attributeName} or {@code null}, if attribute is not available.
   * <p>
   * The attribute node is either a {@link DoValue}, a {@link DoList}, a {@link DoSet} or a {@link DoCollection}
   * wrapper object.
   */
  @Override
  public DoNode<?> getNode(String attributeName) {
    return m_attributes.get(attributeName);
  }

  /**
   * @return {@code true} if attribute with name {@code attributeName} exists (attribute value could be null), otherwise
   * {@code false}
   */
  @Override
  public boolean has(String attributeName) {
    return m_attributes.containsKey(attributeName);
  }

  /**
   * Adds new {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} node to attributes map.
   */
  @Override
  public void putNode(String attributeName, DoNode<?> attribute) {
    IDoEntity.super.putNode(attributeName, attribute);
    m_attributes.put(attributeName, attribute);
  }

  /**
   * Associates the specified value with the specified attribute name in this entity. If the entity previously contained
   * a mapping for the attribute name, the old value is replaced by the specified value wrapped within a
   * {@link DoValue}.
   */
  @Override
  public void put(String attributeName, Object value) {
    if (has(attributeName)) {
      getValueNode(attributeName).set(value);
    }
    else {
      newValueNode(attributeName, value).create();
    }
  }

  /**
   * Associates the specified list value with the specified attribute name in this entity. If the entity previously
   * contained a mapping for the attribute name, the old list value is replaced by the specified value wrapped within a
   * {@link DoList}.
   */
  @Override
  public <V> void putList(String attributeName, List<V> value) {
    if (has(attributeName)) {
      DoList<V> node = getListNode(attributeName);
      node.set(value);
    }
    else {
      newListNode(attributeName, value).create();
    }
  }

  /**
   * Associates the specified set value with the specified attribute name in this entity. If the entity previously
   * contained a mapping for the attribute name, the old set value is replaced by the specified value wrapped within a
   * {@link DoSet}.
   */
  @Override
  public <V> void putSet(String attributeName, Set<V> value) {
    if (has(attributeName)) {
      DoSet<V> node = getSetNode(attributeName);
      node.set(value);
    }
    else {
      newSetNode(attributeName, value).create();
    }
  }

  /**
   * Associates the specified collection value with the specified attribute name in this entity. If the entity
   * previously contained a mapping for the attribute name, the old collection value is replaced by the specified value
   * wrapped within a {@link DoCollection}.
   */
  @Override
  public <V> void putCollection(String attributeName, Collection<V> value) {
    if (has(attributeName)) {
      DoCollection<V> node = getCollectionNode(attributeName);
      node.set(value);
    }
    else {
      newCollectionNode(attributeName, value).create();
    }
  }

  /**
   * Removes {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} attribute from attributes map.
   */
  @Override
  public boolean remove(String attributeName) {
    return m_attributes.remove(attributeName) != null;
  }

  /**
   * Removes all {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} attribute from attributes map
   * that satisfy the given predicate. Errors or runtime exceptions thrown during iteration or by the predicate are
   * relayed to the caller.
   */
  @Override
  public boolean removeIf(Predicate<? super DoNode<?>> filter) {
    return m_attributes.values().removeIf(filter);
  }

  @Override
  public Map<String, DoNode<?>> allNodes() {
    return Collections.unmodifiableMap(m_attributes);
  }

  @Override
  public Map<String, ?> all() {
    return all(Function.identity());
  }

  @Override
  public boolean hasContributions() {
    return !CollectionUtility.isEmpty(m_contributions); // no call to getContributions because internal representation is created otherwise
  }

  @Override
  public Collection<IDoEntityContribution> getContributions() {
    if (m_contributions == null) {
      m_contributions = new ArrayList<>(); // create on first access
    }
    return m_contributions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DoEntity doEntity = (DoEntity) o;

    if (!m_attributes.equals(doEntity.m_attributes)) {
      return false;
    }

    // handle null and empty contributions the same way (lazy init of m_contributions)
    List<IDoEntityContribution> contributions = hasContributions() ? m_contributions : null;
    List<IDoEntityContribution> otherContributions = doEntity.hasContributions() ? doEntity.m_contributions : null;
    if (!CollectionUtility.equalsCollection(contributions, otherContributions, false)) { // element order is not relevant
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = m_attributes.hashCode();
    Collection<? extends IDoEntityContribution> contributions = hasContributions() ? m_contributions : null; // handle null and empty contributions the same way (lazy init of m_contributions)
    result = 31 * result + CollectionUtility.hashCodeCollection(contributions); // element order is not relevant
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + BEANS.get(DataObjectHelper.class).toString(this);
  }

  // ------- helper methods ------- //

  /**
   * Returns the {@link DoValue} attribute node wrapping a value of type {@code V}. A new node is created, if this
   * entity does not already contain a node for the given {@code attributeName}.
   */
  protected <V> DoValue<V> doValue(String attributeName) {
    if (has(attributeName)) {
      return getValueNode(attributeName);
    }
    else {
      return newValueNode(attributeName, null);
    }
  }

  /**
   * Returns the {@link DoList} attribute node wrapping a value of type {@code List<V>}. A new node is created, if this
   * entity does not already contain a node for the given {@code attributeName}.
   */
  protected <V> DoList<V> doList(String attributeName) {
    if (has(attributeName)) {
      return getListNode(attributeName);
    }
    else {
      return newListNode(attributeName, null);
    }
  }

  /**
   * Returns the {@link DoSet} attribute node wrapping a value of type {@code Set<V>}. A new node is created, if this
   * entity does not already contain a node for the given {@code attributeName}.
   */
  protected <V> DoSet<V> doSet(String attributeName) {
    if (has(attributeName)) {
      return getSetNode(attributeName);
    }
    else {
      return newSetNode(attributeName, null);
    }
  }

  /**
   * Returns the {@link DoCollection} attribute node wrapping a value of type {@code Collection<V>}. A new node is
   * created, if this entity does not already contain a node for the given {@code attributeName}.
   */
  protected <V> DoCollection<V> doCollection(String attributeName) {
    if (has(attributeName)) {
      return getCollectionNode(attributeName);
    }
    else {
      return newCollectionNode(attributeName, null);
    }
  }

  protected boolean nvl(Boolean value) {
    return value != null && value.booleanValue();
  }

  /**
   * @return the map of all attribute values mapped using specified {@code mapper} function.
   */
  protected <T> Map<String, T> all(Function<Object, T> mapper) {
    return allNodes().entrySet().stream()
        .collect(StreamUtility.toLinkedHashMap(Entry::getKey, entry -> mapper.apply(entry.getValue().get())));
  }

  /**
   * @return DoNode for given {@code attributeName} having given {@code clazz} type.
   */
  <V, NODE extends DoNode<V>> NODE getNode(String attributeName, Class<NODE> clazz) {
    assertNotNull(attributeName, "attribute name cannot be null");
    DoNode<?> node = getNode(attributeName);
    assertInstance(node, clazz, "Node {} is null or not of type {}", node, clazz);
    //noinspection unchecked
    return (NODE) node;
  }

  /**
   * @return DoValue for given {@code attributeName}.
   */
  <V> DoValue<V> getValueNode(String attributeName) {
    //noinspection unchecked
    return getNode(attributeName, DoValue.class);
  }

  /**
   * @return DoList for given {@code attributeName}.
   */
  <V> DoList<V> getListNode(String attributeName) {
    //noinspection unchecked
    return getNode(attributeName, DoList.class);
  }

  /**
   * @return DoSet for given {@code attributeName}.
   */
  <V> DoSet<V> getSetNode(String attributeName) {
    //noinspection unchecked
    return getNode(attributeName, DoSet.class);
  }

  /**
   * @return DoCollection for given {@code attributeName}.
   */
  <V> DoCollection<V> getCollectionNode(String attributeName) {
    //noinspection unchecked
    return getNode(attributeName, DoCollection.class);
  }

  /**
   * Creates a new {@code DoValue} node using the given {@code initialValue}.
   */
  <V> DoValue<V> newValueNode(String attributeName, V initialValue) {
    return new DoValue<>(attributeName, attribute -> putNode(attributeName, attribute), initialValue);
  }

  /**
   * Creates a new {@code DoCollection} node using the given {@code initialValue}.
   */
  <V> DoCollection<V> newCollectionNode(String attributeName, Collection<V> initialValue) {
    return new DoCollection<>(attributeName, attribute -> putNode(attributeName, attribute), initialValue);
  }

  /**
   * Creates a new {@code DoList} node using the given {@code initialValue}.
   */
  <V> DoList<V> newListNode(String attributeName, List<V> initialValue) {
    return new DoList<>(attributeName, attribute -> putNode(attributeName, attribute), initialValue);
  }

  /**
   * Creates a new {@code DoSet} node using the given {@code initialValue}.
   */
  <V> DoSet<V> newSetNode(String attributeName, Set<V> initialValue) {
    return new DoSet<>(attributeName, attribute -> putNode(attributeName, attribute), initialValue);
  }
}
