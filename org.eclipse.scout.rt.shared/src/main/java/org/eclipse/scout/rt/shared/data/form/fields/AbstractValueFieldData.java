/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fields;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

public abstract class AbstractValueFieldData<T> extends AbstractFormFieldData implements IHolder<T> {
  private static final long serialVersionUID = 1L;

  private T m_value;

  @Override
  public Class<?> getFieldStopClass() {
    return AbstractValueFieldData.class;
  }

  @Override
  public T getValue() {
    return m_value;
  }

  @Override
  public void setValue(T o) {
    m_value = o;
    setValueSet(true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<T> getHolderType() {
    return TypeCastUtility.getGenericsParameterClass(getClass(), IHolder.class);
  }

  /**
   * readObject is implemented to validate potential security attacks that invalidated the value type
   */
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    //verify if valueSet and the type of the value are valid and consistent
    if (!isValueSet()) {
      m_value = null;
    }
    if (m_value == null) {
      return;
    }
    if (!getHolderType().isAssignableFrom(m_value.getClass())) {
      throw new SecurityException("value is of inconsistent type; potential value corruption attack");
    }
  }
}
