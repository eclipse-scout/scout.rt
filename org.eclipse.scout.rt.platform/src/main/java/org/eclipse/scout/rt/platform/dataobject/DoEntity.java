package org.eclipse.scout.rt.platform.dataobject;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

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
 *   public DoValue<Date> createdOn() {
 *     return doValue("createdOn");
 *   }
 *
 *   public DoList<String> nodes() {
 *     return doList("nodes");
 *   }
 * }
 * </pre>
 */
@Bean
public class DoEntity {

  private final Map<String, DoNode<?>> m_attributes = new LinkedHashMap<>();

  /**
   * @return Node of attribute {@code attributeName} or {@code null}, if attribute is not available.
   *         <p>
   *         The attribute node is either a {@link DoValue<T>} or a {@link DoList<T>} wrapper object.
   */
  public DoNode<?> getNode(String attributeName) {
    return m_attributes.get(attributeName);
  }

  /**
   * @return {@link Optional} of node with attribute {@code attributeName} or empty {@link Optional}, if attribute is
   *         not available.
   *         <p>
   *         The attribute node is either a {@link DoValue<T>} or a {@link DoList<T>} wrapper object.
   */
  public Optional<DoNode<?>> optNode(String attributeName) {
    return Optional.ofNullable(getNode(attributeName));
  }

  /**
   * @return Value of attribute {@code attributeName} or {@code null}, if attribute is not available.
   * @see DoEntity#getNode(String) to get the wrapped {@link DoNode} attribute
   */
  public Object get(String attributeName) {
    DoNode<?> node = getNode(attributeName);
    if (node != null) {
      return node.get();
    }
    return null;
  }

  /**
   * @return Value of attribute {@code attributeName} casted to specified {@code type} or {@code null} if attribute is
   *         not available.
   * @see DoEntity#getNode(String) to get the wrapped {@link DoNode} attribute
   */
  public <T> T get(String attributeName, Class<T> type) {
    Assertions.assertNotNull(type, "provided type is null");
    return type.cast(get(attributeName));
  }

  /**
   * @return Value of attribute {@code attributeName} mapped to a custom type using specified {@code mapper} or
   *         {@code null} if attribute is not available.
   */
  public <T> T get(String attributeName, Function<Object, T> mapper) {
    Assertions.assertNotNull(mapper, "provided mapper function is null");
    return mapper.apply(get(attributeName));
  }

  /**
   * @return List value of attribute {@code attributeName} or {@code null} if attribute is not available.
   * @see DoEntity#getNode(String) to get the wrapped attribute node
   */
  public List<Object> getList(String attributeName) {
    return getList(attributeName, Object.class);
  }

  /**
   * @return List value of attribute {@code attributeName} casted to specified {@code type} or {@code null} if attribute
   *         is not available.
   * @see DoEntity#getNode(String) to get the wrapped attribute node
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(String attributeName, Class<T> type) {
    return optNode(attributeName)
        .map(n -> Assertions.assertType(n, DoList.class).get())
        .orElse(null);
  }

  /**
   * @return Value of list attribute {@code attributeName}, each element mapped to a custom type using specified
   *         {@code mapper} or {@code null} if attribute is not available.
   */
  public <T> List<T> getList(String attributeName, Function<Object, T> mapper) {
    Assertions.assertNotNull(mapper, "provided mapper function is null");
    return getList(attributeName).stream().map(item -> mapper.apply(item)).collect(Collectors.toList());
  }

  /**
   * @return Value of attribute {@code attributeName} converted to {@link BigDecimal} or {@code null} if attribute is
   *         not available.
   * @throws AssertionException
   *           if attribute value is not instance of {@link BigDecimal}
   */
  public BigDecimal getDecimal(String attributeName) {
    return TypeCastUtility.castValue(Assertions.assertType(get(attributeName), Number.class), BigDecimal.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} converted to a list of {@link BigDecimal} or {@code null} if
   *         attribute is not available.
   * @throws AssertionException
   *           if a list item value is not instance of {@link Number}
   */
  public List<BigDecimal> getDecimalList(String attributeName) {
    return getList(attributeName, item -> TypeCastUtility.castValue(Assertions.assertType(item, Number.class), BigDecimal.class));
  }

  /**
   * @return Value of attribute {@code attributeName} casted to {@link Boolean} or {@code null} if attribute is not
   *         available.
   * @throws AssertionException
   *           if attribute value is not instance of {@link Boolean}
   */
  public Boolean getBoolean(String attributeName) {
    return Assertions.assertType(get(attributeName), Boolean.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} casted to {@link List<Boolean>} or {@code null} if attribute
   *         is not available.
   * @throws AssertionException
   *           if a list item value is not instance of {@link Boolean}
   */
  public List<Boolean> getBooleanList(String attributeName) {
    return getList(attributeName, item -> Assertions.assertType(item, Boolean.class));
  }

  /**
   * @return Value of attribute {@code attributeName} casted to {@link String} or {@code null} if attribute is not
   *         available.
   * @throws AssertionException
   *           if attribute value is not instance of {@link String}
   */
  public String getString(String attributeName) {
    return Assertions.assertType(get(attributeName), String.class);
  }

  /**
   * @return Value of list attribute {@code attributeName} casted to {@link String} or {@code null} if attribute is not
   *         available.
   * @throws AssertionException
   *           if a list item value is not instance of {@link String}
   */
  public List<String> getStringList(String attributeName) {
    return getList(attributeName, item -> Assertions.assertType(item, String.class));
  }

  /**
   * @return {@code true} if attribute with name {@code attributeName} exists (attribute value could be null), otherwise
   *         {@code false}
   */
  public boolean has(String attributeName) {
    return m_attributes.containsKey(attributeName);
  }

  /**
   * Adds new {@link DoValue} or {@link DoList} node to attributes map.
   */
  public void putNode(String attributeName, DoNode<?> attribute) {
    Assertions.assertNotNull(attributeName, "attribute name cannot be null");
    Assertions.assertNotNull(attribute, "attribute node cannot be null for attribute name {}", attributeName);
    attribute.setAttributeName(attributeName);
    m_attributes.put(attributeName, attribute);
  }

  /**
   * Adds new value to attribute map. The value is wrapped within a {@link DoValue}.
   */
  public void put(String attributeName, Object value) {
    doValue(attributeName).set(value);
  }

  /**
   * Adds new list value to attribute map. The value is wrapped within a {@link DoList}.
   */
  public void putList(String attributeName, List<Object> value) {
    doList(attributeName).set(value);
  }

  /**
   * Removes {@link DoValue} or {@link DoList} attribute from attributes map.
   */
  public void remove(String attributeName) {
    m_attributes.remove(attributeName);
  }

  /**
   * @return the read-only map of all attributes.
   */
  public Map<String, DoNode<?>> all() {
    return Collections.unmodifiableMap(m_attributes);
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
