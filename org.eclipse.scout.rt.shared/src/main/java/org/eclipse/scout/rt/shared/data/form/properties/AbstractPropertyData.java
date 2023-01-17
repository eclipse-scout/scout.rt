/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.properties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.form.FormDataUtility;

public abstract class AbstractPropertyData<T> implements IHolder<T>, Serializable {

  private static final Pattern PROPERTY_SUFFIX = Pattern.compile("Property$");
  private static final long serialVersionUID = 1L;

  private T m_value;
  private boolean m_valueSet;

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

  public boolean isValueSet() {
    return m_valueSet;
  }

  public void setValueSet(boolean b) {
    m_valueSet = b;
  }

  public String getPropertyId() {
    String s = getClass().getSimpleName();
    if (s.endsWith("Property")) {
      s = PROPERTY_SUFFIX.matcher(s).replaceAll("");
    }
    return s;
  }

  /**
   * readObject is implemented to validate potential security attacks that invalidated the value type
   */
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    //verify if valueSet and the type of the value are valid and consistent
    if (!m_valueSet) {
      m_value = null;
    }
    if (m_value == null) {
      return;
    }
    if (!getHolderType().isAssignableFrom(m_value.getClass())) {
      throw new SecurityException("value is of inconsistent type; potential value corruption attack");
    }
  }

  @Override
  public String toString() {
    return FormDataUtility.toString(this, false);
  }
}
