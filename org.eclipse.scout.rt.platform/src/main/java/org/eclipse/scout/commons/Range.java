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

public class Range<VALUE_TYPE> implements Serializable {

  private static final long serialVersionUID = 1L;

  private VALUE_TYPE m_from;
  private VALUE_TYPE m_to;

  public Range() {
    this(null, null);
  }

  public Range(VALUE_TYPE from, VALUE_TYPE to) {
    m_from = from;
    m_to = to;
  }

  public Range(Range<VALUE_TYPE> rangeToCopy) {
    this(rangeToCopy != null ? rangeToCopy.getFrom() : null, rangeToCopy != null ? rangeToCopy.getTo() : null);
  }

  public void setFrom(VALUE_TYPE from) {
    m_from = from;
  }

  public VALUE_TYPE getFrom() {
    return m_from;
  }

  public void setTo(VALUE_TYPE to) {
    m_to = to;
  }

  public VALUE_TYPE getTo() {
    return m_to;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_from == null) ? 0 : m_from.hashCode());
    result = prime * result + ((m_to == null) ? 0 : m_to.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Range other = (Range) obj;
    if (m_from == null) {
      if (other.m_from != null) {
        return false;
      }
    }
    else if (!m_from.equals(other.m_from)) {
      return false;
    }
    if (m_to == null) {
      if (other.m_to != null) {
        return false;
      }
    }
    else if (!m_to.equals(other.m_to)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Range [m_from=" + m_from + ", m_to=" + m_to + "]";
  }

}
