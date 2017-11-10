/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.data.query;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class ChunkRange implements Serializable {
  private static final long serialVersionUID = 1L;

  private long m_from = -1;
  private long m_to = -1;

  public ChunkRange() {
    // no range
  }

  public ChunkRange(long from, long to) {
    m_from = from;
    m_to = to;
  }

  public ChunkRange(ChunkRange other) {
    if (other != null) {
      m_from = other.getFrom();
      m_to = other.getTo();
    }
  }

  public long getFrom() {
    return m_from;
  }

  public void setFrom(long from) {
    m_from = from;
  }

  public long getTo() {
    return m_to;
  }

  public void setTo(long to) {
    m_to = to;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (m_from ^ (m_from >>> 32));
    result = prime * result + (int) (m_to ^ (m_to >>> 32));
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
    ChunkRange other = (ChunkRange) obj;
    if (m_from != other.m_from) {
      return false;
    }
    if (m_to != other.m_to) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr("from", m_from)
        .attr("to", m_to)
        .toString();
  }
}
