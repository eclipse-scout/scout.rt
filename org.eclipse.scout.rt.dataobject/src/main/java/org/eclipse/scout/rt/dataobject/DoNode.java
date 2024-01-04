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

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * This is the base type for all building blocks of a data object such as {@link DoValue}, {@link DoList},
 * {@link DoSet} and {@link DoCollection}.
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
   * Set value of this node.
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
   * Calls {@link Consumer#accept(Object)} if node exists.
   */
  public final void ifPresent(Consumer<T> consumer) {
    if (exists()) {
      consumer.accept(get());
    }
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    // treat exists() (e.g. member variable m_lazyCreate) as boolean value (null/not null) for hash code
    result = prime * result + (exists() ? 1231 : 1237);
    result = prime * result + ((m_attributeName == null) ? 0 : m_attributeName.hashCode());
    result = prime * result + valueHashCode();
    return result;
  }

  /**
   * Returns a hash code value for {@link #m_value}.
   * <p>
   * Subclasses might need to override this method in order to provide a more suitable hashcode implementation then the
   * default one. If overridden, make sure to override {@link #valueEquals(DoNode)} too.
   */
  protected int valueHashCode() {
    return (m_value == null) ? 0 : m_value.hashCode();
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
    DoNode other = (DoNode) obj;
    // treat exists() (e.g. member variable m_lazyCreate) as boolean value (null/not null) for equality
    if (exists() != other.exists()) {
      return false;
    }
    if (m_attributeName == null) {
      if (other.m_attributeName != null) {
        return false;
      }
    }
    else if (!m_attributeName.equals(other.m_attributeName)) {
      return false;
    }
    return valueEquals(other);
  }

  /**
   * Indicates whether {@link #m_value} is equal to the value of the other node.
   * <p>
   * Subclasses might need to override this method in order to provide a more suitable equals implementation then the
   * default one. If overridden, make sure to override {@link #valueHashCode()} too.
   */
  protected boolean valueEquals(DoNode other) {
    return ObjectUtility.equals(m_value, other.m_value);
  }
}
