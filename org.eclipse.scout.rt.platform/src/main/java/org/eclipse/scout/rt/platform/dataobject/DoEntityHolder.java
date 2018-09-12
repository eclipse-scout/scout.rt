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
package org.eclipse.scout.rt.platform.dataobject;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Serializable {@link IHolder} implementation for {@link DoEntity} subclasses
 */
public class DoEntityHolder<T extends IDoEntity> implements IHolder<T>, Serializable {
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
    T value = (T) BEANS.get(IDataObjectMapper.class).readValue(text, IDoEntity.class);
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
