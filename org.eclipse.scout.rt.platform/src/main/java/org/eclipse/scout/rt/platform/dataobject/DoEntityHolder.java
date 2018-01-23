/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Serializable {@link IHolder} implementation for {@link DoEntity} subclasses
 */
public class DoEntityHolder<T extends DoEntity> implements IHolder<T>, Serializable {
  private static final long serialVersionUID = 1L;

  private transient T m_value;
  private final Class<T> m_clazz;

  public DoEntityHolder() {
    this(null);
  }

  public DoEntityHolder(Class<T> clazz) {
    m_clazz = clazz;
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
      clazz = TypeCastUtility.getGenericsParameterClass(this.getClass(), DoEntityHolder.class);
    }
    return clazz;
  }

  /**
   * Custom java object deserialization method using {@link IDataObjectMapper} to serialize {@link DoEntity} to a string
   * value instead of relying on default java object serialization (which would require {@link DoEntity} to implement
   * the {@link Serializable} interface).
   */
  private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
    String text = (String) stream.readObject();
    @SuppressWarnings("unchecked")
    T value = (T) BEANS.get(IDataObjectMapper.class).readValue(text, DoEntity.class);
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
