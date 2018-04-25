package org.eclipse.scout.rt.platform.dataobject;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * This is the base type for all building blocks of of a data object such as {@link DoValue} and {@link DoList}.
 */
public class DoNode<T> {

  private String m_attributeName;
  private Consumer<DoNode<T>> m_lazyCreate;
  private T m_value;

  protected DoNode(String attributeName, Consumer<DoNode<T>> lazyCreate, T initialValue) {
    m_attributeName = attributeName;
    m_lazyCreate = lazyCreate;
    m_value = initialValue;
  }

  /**
   * @return {@code true} if this attribute is part of a {@link DoEntity}, otherwise {@code false}.
   */
  public final boolean exists() {
    return m_lazyCreate == null;
  }

  /**
   * Marks this attribute to be part of a {@link DoEntity}.
   */
  public final DoNode<T> create() {
    if (m_lazyCreate != null) {
      m_lazyCreate.accept(this);
      m_lazyCreate = null;
    }
    return this;
  }

  /**
   * @return value of type {@code T} of this node
   */
  public T get() {
    return m_value;
  }

  /**
   * @return set value of this node
   */
  public void set(T newValue) {
    create();
    m_value = newValue;
  }

  /**
   * @return An {@code Optional} describing the wrapped {@link DoNode} value, if the wrapped value within this
   *         {@link DoNode} is non-null, and this {@link DoNode} is part of a {@link DoEntity}. Otherwise returns an
   *         empty {@code Optional}.<br>
   *         Note: Being part of a {@link DoEntity} means, that {@link DoNode#exists()} returns {@code true}.
   */
  public final Optional<T> toOptional() {
    if (exists()) {
      return Optional.ofNullable(get());
    }
    return Optional.empty();
  }

  /**
   * Internal method used to set attribute name when the node is added to a {@link DoEntity} object.
   */
  protected final void setAttributeName(String attributeName) {
    m_attributeName = attributeName;
  }

  /**
   * Return the attribute name, if this node is used within a {@link DoEntity} object.
   */
  public final String getAttributeName() {
    return m_attributeName;
  }
}
