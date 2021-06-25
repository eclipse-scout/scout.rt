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
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
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
  private final List<IDoEntityContribution> m_contributions = new ArrayList<>();

  /**
   * @return Node of attribute {@code attributeName} or {@code null}, if attribute is not available.
   *         <p>
   *         The attribute node is either a {@link DoValue}, a {@link DoList}, a {@link DoSet} or a {@link DoCollection}
   *         wrapper object.
   */
  @Override
  public DoNode<?> getNode(String attributeName) {
    return m_attributes.get(attributeName);
  }

  /**
   * @return {@code true} if attribute with name {@code attributeName} exists (attribute value could be null), otherwise
   *         {@code false}
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
   * Adds new value to attribute map. The value is wrapped within a {@link DoValue}.
   */
  @Override
  public void put(String attributeName, Object value) {
    doValue(attributeName).set(value);
  }

  /**
   * Adds new list value to attribute map. The value is wrapped within a {@link DoList}.
   */
  @Override
  public <V> void putList(String attributeName, List<V> value) {
    DoList<V> doList = doList(attributeName);
    doList.set(value);
  }

  /**
   * Adds new set value to attribute map. The value is wrapped within a {@link DoSet}.
   */
  @Override
  public <V> void putSet(String attributeName, Set<V> value) {
    DoSet<V> doSet = doSet(attributeName);
    doSet.set(value);
  }

  /**
   * Adds new collection value to attribute map. The value is wrapped within a {@link DoCollection}.
   */
  @Override
  public <V> void putCollection(String attributeName, Collection<V> value) {
    DoCollection<V> doCollection = doCollection(attributeName);
    doCollection.set(value);
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

  /**
   * @return the map of all attribute values mapped using specified {@code mapper} function.
   */
  protected <T> Map<String, T> all(Function<Object, T> mapper) {
    return allNodes().entrySet().stream()
        .collect(StreamUtility.toLinkedHashMap(Entry::getKey, entry -> mapper.apply(entry.getValue().get())));
  }

  /**
   * Creates a new {@link DoValue} value attribute node wrapping a value of type {@code V}
   */
  protected <V> DoValue<V> doValue(String attributeName) {
    //noinspection unchecked
    return doNode(attributeName, DoValue.class, () -> new DoValue<V>(attributeName, attribute -> putNode(attributeName, attribute)));
  }

  /**
   * Creates a new {@link DoList} list value attribute node wrapping a list of type {@code List<V>}
   */
  protected <V> DoList<V> doList(String attributeName) {
    //noinspection unchecked
    return doNode(attributeName, DoList.class, () -> new DoList<V>(attributeName, attribute -> putNode(attributeName, attribute)));
  }

  /**
   * Creates a new {@link DoSet} set value attribute node wrapping a list of type {@code Set<V>}
   */
  protected <V> DoSet<V> doSet(String attributeName) {
    //noinspection unchecked
    return doNode(attributeName, DoSet.class, () -> new DoSet<V>(attributeName, attribute -> putNode(attributeName, attribute)));
  }

  /**
   * Creates a new {@link DoCollection} collection value attribute node wrapping a list of type {@code Collection<V>}
   */
  protected <V> DoCollection<V> doCollection(String attributeName) {
    //noinspection unchecked
    return doNode(attributeName, DoCollection.class, () -> new DoCollection<V>(attributeName, attribute -> putNode(attributeName, attribute)));
  }

  protected <NODE> NODE doNode(String attributeName, Class<NODE> clazz, Supplier<NODE> nodeSupplier) {
    assertNotNull(attributeName, "attribute name cannot be null");
    DoNode<?> node = getNode(attributeName);
    if (node != null) {
      Assertions.assertInstance(node, clazz, "Existing node {} is not of type {}, cannot change the node type!", node, clazz);
      //noinspection unchecked
      return (NODE) node;
    }
    return nodeSupplier.get();
  }

  @Override
  public Collection<IDoEntityContribution> getContributions() {
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
    if (m_attributes != null ? !m_attributes.equals(doEntity.m_attributes) : doEntity.m_attributes != null) {
      return false;
    }
    if (!CollectionUtility.equalsCollection(m_contributions, doEntity.m_contributions, false)) { // element order is not relevant
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = m_attributes != null ? m_attributes.hashCode() : 0;
    result = 31 * result + CollectionUtility.hashCodeCollection(m_contributions); // element order is not relevant
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + BEANS.get(DataObjectHelper.class).toString(this);
  }
}
