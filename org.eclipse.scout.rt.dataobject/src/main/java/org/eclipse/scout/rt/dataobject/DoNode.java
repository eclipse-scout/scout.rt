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

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.ObjectUtility;

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    // treat exists() (e.g. member variable m_lazyCreate) as boolean value (null/not null) for hash code
    result = prime * result + (exists() ? 1231 : 1237);
    result = prime * result + ((m_attributeName == null) ? 0 : m_attributeName.hashCode());
    result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
    return ObjectUtility.equals(m_value, other.m_value);
  }
}
