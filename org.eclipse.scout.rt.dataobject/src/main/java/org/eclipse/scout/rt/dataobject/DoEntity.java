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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
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
   *         The attribute node is either a {@link DoValue<T>} or a {@link DoList<T>} wrapper object.
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
   * Removes {@link DoValue} or {@link DoList} attribute from attributes map.
   */
  @Override
  public void remove(String attributeName) {
    m_attributes.remove(attributeName);
  }

  /**
   * Removes all {@link DoValue} or {@link DoList} attribute from attributes map that satisfy the given predicate.
   * Errors or runtime exceptions thrown during iteration or by the predicate are relayed to the caller.
   */
  @Override
  public void removeIf(Predicate<? super DoNode<?>> filter) {
    m_attributes.values().removeIf(filter);
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
    Assertions.assertNotNull(attributeName, "attribute name cannot be null");
    DoNode<?> node = getNode(attributeName);
    if (node != null) {
      Assertions.assertInstance(node, DoValue.class, "Existing node {} is not of type {}, cannot change the node type!", node, DoValue.class);
      @SuppressWarnings("unchecked")
      DoValue<V> valueNode = (DoValue<V>) node;
      return valueNode;
    }
    return new DoValue<>(attributeName, attribute -> putNode(attributeName, attribute));
  }

  /**
   * Creates a new {@link DoList} list value attribute node wrapping a list of type {@code List<V>}
   */
  protected <V> DoList<V> doList(String attributeName) {
    Assertions.assertNotNull(attributeName, "attribute name cannot be null");
    DoNode<?> node = getNode(attributeName);
    if (node != null) {
      Assertions.assertInstance(node, DoList.class, "Existing node {} is not of type {}, cannot change the node type!", node, DoList.class);
      @SuppressWarnings("unchecked")
      DoList<V> listNode = (DoList<V>) node;
      return listNode;
    }
    return new DoList<>(attributeName, attribute -> putNode(attributeName, attribute));
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
