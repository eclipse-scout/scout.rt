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
package org.eclipse.scout.commons.holders;

import java.io.Serializable;

/**
 * @since 3.0
 */

public class Holder<T> implements IHolder<T>, Serializable {
  private static final long serialVersionUID = 1L;
  private T m_value;
  private final Class<T> m_clazz;

  public Holder(Class<T> clazz) {
    m_clazz = clazz;
  }

  public Holder(Class<T> clazz, T o) {
    m_value = o;
    m_clazz = clazz;
  }

  @Override
  public T getValue() {
    return m_value;
  }

  @Override
  public void setValue(T o) {
    synchronized (this) {
      m_value = o;
      this.notifyAll();
    }
  }

  @Override
  public Class<T> getHolderType() {
    return m_clazz;
  }

  @Override
  public String toString() {
    return "Holder<" + m_clazz.getSimpleName() + ">(" + (m_value == null ? "" : m_value.toString()) + ")";
  }
}
