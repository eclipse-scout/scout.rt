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
public class CompositeLong implements Comparable, Serializable {
  private static final long serialVersionUID = 0L;
  private long[] m_value;

  public CompositeLong(long[] a) {
    m_value = a;
  }

  public CompositeLong(long a) {
    m_value = new long[]{a};
  }

  public CompositeLong(long a, long b) {
    m_value = new long[]{a, b};
  }

  public CompositeLong(long a, long b, long c) {
    m_value = new long[]{a, b, c};
  }

  public CompositeLong(long a, long b, long c, long d) {
    m_value = new long[]{a, b, c, d};
  }

  @Override
  public int hashCode() {
    long h = 0;
    for (int i = 0; i < m_value.length; i++) {
      h = h ^ m_value[i];
    }
    return (int) h;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    return compareTo(o) == 0;
  }

  public int compareTo(Object o) {
    long[] me = this.m_value;
    long[] other = ((CompositeLong) o).m_value;
    for (int i = 0; i < me.length && i < other.length; i++) {
      if (me[i] < other[i]) return -1;
      if (me[i] > other[i]) return 1;
    }
    if (me.length < other.length) return -1;
    if (me.length > other.length) return 1;
    return 0;
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
