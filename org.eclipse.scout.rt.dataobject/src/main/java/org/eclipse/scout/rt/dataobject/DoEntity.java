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

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.BEANS;
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

  /**
   * @return Node of attribute {@code attributeName} or {@code null}, if attribute is not available.
   *         <p>
   *         The attribute node is either a {@link DoValue} or a {@link DoList} wrapper object.
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
   * Adds new {@link DoValue} or {@link DoList} node to attributes map.
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
      DoList<V> list = getListNode(attributeName);
      list.set(value);
    }
    else {
      newListNode(attributeName, value).create();
    }
  }

  /**
   * Removes {@link DoValue} or {@link DoList} attribute from attributes map.
   */
  @Override
  public boolean remove(String attributeName) {
    return m_attributes.remove(attributeName) != null;
  }

  /**
   * Removes all {@link DoValue} or {@link DoList} attribute from attributes map that satisfy the given predicate.
   * Errors or runtime exceptions thrown during iteration or by the predicate are relayed to the caller.
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
   * Returns the {@link DoValue} attribute node wrapping a value of type {@code V}. A new node is created, if this
   * entity does not already contain a node for the given {@code attributName}.
   */
  protected <V> DoValue<V> doValue(String attributeName) {
    if (has(attributeName)) {
      return getValueNode(attributeName);
    }
    return newValueNode(attributeName, null);
  }

  /**
   * Returns the {@link DoValue} attribute node wrapping a value of type {@code V}.
   */
  <V> DoValue<V> getValueNode(String attributeName) {
    assertNotNull(attributeName, "attribute name cannot be null");
    DoNode<?> node = getNode(attributeName);
    assertInstance(node, DoValue.class, "Existing node {} is null or not of type {}", node, DoValue.class);
    @SuppressWarnings("unchecked")
    DoValue<V> valueNode = (DoValue<V>) node;
    return valueNode;
  }

  /**
   * Creates a new {@code DoValue} node using the given {@code initialValue}.
   */
  <V> DoValue<V> newValueNode(String attributeName, V initialValue) {
    return new DoValue<>(attributeName, attribute -> putNode(attributeName, attribute), initialValue);
  }

  /**
   * Returns the {@link DoList} attribute node wrapping a value of type {@code List<V>}. A new node is created, if this
   * entity does not already contain a node for the given {@code attributName}.
   */
  protected <V> DoList<V> doList(String attributeName) {
    if (has(attributeName)) {
      return getListNode(attributeName);
    }
    return newListNode(attributeName, null);
  }

  /**
   * Returns the {@link DoList} attribute node wrapping a value of type {@code List<V>}.
   */
  <V> DoList<V> getListNode(String attributeName) {
    assertNotNull(attributeName, "attribute name cannot be null");
    DoNode<?> node = getNode(attributeName);
    assertInstance(node, DoList.class, "Existing node {} is null or not of type {}", node, DoList.class);
    @SuppressWarnings("unchecked")
    DoList<V> listNode = (DoList<V>) node;
    return listNode;
  }

  /**
   * Creates a new {@code DoList} node using the given {@code initialValue}.
   */
  <V> DoList<V> newListNode(String attributeName, List<V> initialValue) {
    return new DoList<>(attributeName, attribute -> putNode(attributeName, attribute), initialValue);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_attributes.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DoEntity other = (DoEntity) obj;
    return m_attributes.equals(other.m_attributes);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + BEANS.get(DataObjectHelper.class).toString(this);
  }
}
