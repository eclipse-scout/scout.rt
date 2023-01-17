/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.holders;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * @since 3.0
 */

public class Holder<T> implements IHolder<T>, Serializable {
  private static final long serialVersionUID = 1L;
  private volatile T m_value; // volatile because modified/read by different threads.
  private final Class<T> m_clazz;

  public Holder() {
    this(null, null);
  }

  public Holder(T o) {
    this(null, o);
  }

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

  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getHolderType() {
    Class<T> clazz = m_clazz;
    if (clazz == null) {
      clazz = TypeCastUtility.getGenericsParameterClass(this.getClass(), IHolder.class);
    }
    return clazz;
  }

  @Override
  @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Holder<");
    Class<T> holderType = getHolderType();
    if (holderType != null) {
      builder.append(holderType.getSimpleName());
    }
    else {
      builder.append("not available");
    }
    builder.append(">(");
    if (getValue() != null) {
      builder.append(getValue().toString());
    }
    builder.append(")");

    return builder.toString();
  }
}
