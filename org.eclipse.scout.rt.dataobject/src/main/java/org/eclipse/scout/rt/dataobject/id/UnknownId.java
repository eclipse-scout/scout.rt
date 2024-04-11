/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import java.util.Objects;

import org.eclipse.scout.rt.dataobject.ILenientDataObjectMapper;

/**
 * Marker class used as value holder class when deserializing IIds having an unknown {@link IdTypeName} using lenient
 * deserialization. This class is used by {@link IdCodec} during lenient deserialization to retain the deserialized raw
 * value before a later executed value migrations is able to change the value and instantiate the corresponding id class.
 *
 * @see IdCodec#fromQualifiedLenient(String)
 * @see ILenientDataObjectMapper
 */
public final class UnknownId implements IId {
  private static final long serialVersionUID = 1L;

  private final String m_idTypeName;
  private final String m_id;

  private UnknownId(String idTypeName, String id) {
    m_idTypeName = idTypeName;
    m_id = id;
  }

  /**
   * <b>Do not create {@link UnknownId} instances manually.</b><br>
   * This method is solely used by {@link IdCodec} during lenient deserialization.
   */
  @Deprecated
  public static UnknownId of(String idTypeName, String id) {
    return new UnknownId(idTypeName, id);
  }

  public String getId() {
    return m_id;
  }

  public String getIdTypeName() {
    return m_idTypeName;
  }

  @Override
  public Object unwrap() {
    return m_id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UnknownId unknownId = (UnknownId) o;
    return Objects.equals(m_idTypeName, unknownId.m_idTypeName) && Objects.equals(m_id, unknownId.m_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_idTypeName, m_id);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + m_idTypeName + ":" + m_id + "]";
  }
}
