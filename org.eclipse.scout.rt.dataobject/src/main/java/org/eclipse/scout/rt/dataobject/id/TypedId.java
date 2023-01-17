/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

/**
 * Wrapper for {@code IId}, used to preserve the type information during serialization.
 *
 * @see IdFactory
 * @see TypedIdSerializer
 * @see TypedIdDeserializer
 */
public class TypedId<ID extends IId> {

  private ID m_id;

  /**
   * @return new {@link TypedId} instance wrapping the given {@code id}
   */
  public static <ID extends IId> TypedId<ID> of(ID id) {
    return new TypedId<ID>().withId(id);
  }

  public TypedId<ID> withId(ID id) {
    m_id = id;
    return this;
  }

  public ID getId() {
    return m_id;
  }

  @Override
  public int hashCode() {
    return 31 + ((m_id == null) ? 0 : m_id.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TypedId<?> other = (TypedId<?>) obj;
    if (m_id == null) {
      if (other.m_id != null) {
        return false;
      }
    }
    else if (!m_id.equals(other.m_id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return TypedId.class.getSimpleName() + "[id=" + m_id + "]";
  }
}
