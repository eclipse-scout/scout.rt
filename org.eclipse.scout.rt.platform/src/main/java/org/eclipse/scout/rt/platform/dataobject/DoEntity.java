package org.eclipse.scout.rt.platform.dataobject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

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
    return new DoValue<>(attribute -> putNode(attributeName, attribute));
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
    return new DoList<>(attribute -> putNode(attributeName, attribute));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + BEANS.get(DataObjectHelper.class).toString(this);
  }
}
