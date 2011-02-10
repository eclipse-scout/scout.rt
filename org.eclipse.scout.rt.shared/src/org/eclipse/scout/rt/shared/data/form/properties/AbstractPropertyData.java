/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.form.properties;

import java.io.Serializable;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.holders.IHolder;

public abstract class AbstractPropertyData<T> implements IHolder<T>, Serializable {
  private static final long serialVersionUID = 1L;

  private T m_value;
  private boolean m_valueSet;

  public AbstractPropertyData() {
    super();
  }

  public T getValue() {
    return m_value;
  }

  public void setValue(T o) {
    m_value = o;
    setValueSet(true);
  }

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
      s = s.replaceAll("Property$", "");
    }
    return s;
  }

}
