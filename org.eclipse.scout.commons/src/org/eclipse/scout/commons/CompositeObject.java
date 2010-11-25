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
package org.eclipse.scout.commons;

import java.io.Serializable;

/**
 * used in sorted maps and sets when dealing with composite sorting criteria
 */
public class CompositeObject implements Comparable<CompositeObject>, Serializable {
  private static final long serialVersionUID = 0L;
  private Object[] m_value;

  public CompositeObject(Object... a) {
    m_value = a;
  }

  @Override
  public int hashCode() {
    long h = 0;
    for (Object v : m_value) {
      if (v != null) {
        h = h ^ v.hashCode();
      }
    }
    return (int) h;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CompositeObject)) return false;
    return compareTo((CompositeObject) o) == 0;
  }

  public int getComponentCount() {
    return m_value.length;
  }

  public Object getComponent(int index) {
    return m_value[index];
  }

  public Object[] getComponents() {
    return m_value;
  }

  public int compareTo(CompositeObject o) {
    Object[] me = this.m_value;
    Object[] other = o.m_value;
    for (int i = 0; i < me.length && i < other.length; i++) {
      int c = compareImpl(me[i], other[i]);
      if (c != 0) return c;
    }
    if (me.length < other.length) return -1;
    if (me.length > other.length) return 1;
    return 0;
  }

  @SuppressWarnings("unchecked")
  private int compareImpl(Object a, Object b) {
    if (a == null && b == null) return 0;
    if (a == null) return -1;
    if (b == null) return 1;
    if ((a instanceof Comparable) && (b instanceof Comparable)) return ((Comparable) a).compareTo(b);
    return a.toString().compareTo(b.toString());
  }

  @Override
  public String toString() {
    String s = "[";
    for (int i = 0; i < m_value.length; i++) {
      s += String.valueOf(m_value[i]);
      if (i + 1 < m_value.length) s += ",";
    }
    s += "]";
    return s;
  }
}
