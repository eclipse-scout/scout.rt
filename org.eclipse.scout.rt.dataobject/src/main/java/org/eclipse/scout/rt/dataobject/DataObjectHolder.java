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

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Serializable {@link IHolder} implementation for {@link IDataObject} subclasses.
 * <p>
 * To hold specific types extending <code>{@link IDoEntity}</code> use the sub class.
 *
 * @see DoEntityHolder
 */
public class DataObjectHolder<T extends IDataObject> implements IHolder<T>, Serializable {

  private static final long serialVersionUID = 1L;

  private T m_value;
  private final Class<T> m_clazz;

  public DataObjectHolder() {
    this(null);
  }

  public DataObjectHolder(Class<T> clazz) {
    this(clazz, null);
  }

  public DataObjectHolder(Class<T> clazz, T value) {
    m_clazz = clazz;
    m_value = value;
  }

  @Override
  public T getValue() {
    return m_value;
  }

  @Override
  public void setValue(T value) {
    m_value = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getHolderType() {
    Class<T> clazz = m_clazz;
    if (clazz == null) {
      clazz = TypeCastUtility.getGenericsParameterClass(this.getClass(), DataObjectHolder.class);
    }
    return clazz;
  }

  /**
   * Custom java object deserialization method using {@link IDataObjectMapper} to serialize {@link IDataObject} to a string
   * value instead of relying on default java object serialization (which would require {@link IDataObject} to implement
   * the {@link Serializable} interface).
   */
  private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
    String text = (String) stream.readObject();
    @SuppressWarnings("unchecked")
    T value = (T) BEANS.get(IDataObjectMapper.class).readValue(text, IDataObject.class);
    setValue(value);
  }

  /**
   * Custom java serialization method based on {@link IDataObjectMapper} instead of default java object serialization.
   */
  private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
    String text = BEANS.get(IDataObjectMapper.class).writeValue(getValue());
    stream.writeObject(text);
  }
}
