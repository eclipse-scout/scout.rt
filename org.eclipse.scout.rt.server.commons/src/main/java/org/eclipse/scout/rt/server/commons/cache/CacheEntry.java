/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.cache;

import java.io.Serializable;

final class CacheEntry<T> implements ICacheEntry<T>, Serializable {
  private static final long serialVersionUID = 1L;
  private final T m_value;
  private long m_creationTime;
  private long m_expiration;

  CacheEntry(T value, long expiration, long creationTime) {
    m_creationTime = creationTime;
    m_value = value;
    m_expiration = expiration;
  }

  /**
   * Element to be stored in the cache
   * 
   * @param value
   *          to be stored
   * @param expiration
   *          time in seconds
   */
  CacheEntry(T value, Long expiration) {
    this(value, expiration, System.currentTimeMillis());
  }

  @Override
  public boolean isActive() {
    return (m_creationTime + m_expiration > System.currentTimeMillis());
  }

  @Override
  public T getValue() {
    return m_value;
  }

  @Override
  public void setExpiration(Long expiration) {
    m_expiration = expiration;
  }

  public long getCreationTime() {
    return m_creationTime;
  }

  @Override
  public void touch() {
    m_creationTime = System.currentTimeMillis();
  }

  @Override
  public String toString() {
    return "CacheElement [m_value=" + m_value + ", m_creationTime=" + m_creationTime + ", m_expiration=" + m_expiration + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (m_creationTime ^ (m_creationTime >>> 32));
    result = prime * result + (int) (m_expiration ^ (m_expiration >>> 32));
    result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
    CacheEntry other = (CacheEntry) obj;
    if (m_creationTime != other.m_creationTime) {
      return false;
    }
    if (m_expiration != other.m_expiration) {
      return false;
    }
    if (m_value == null) {
      if (other.m_value != null) {
        return false;
      }
    }
    else if (!m_value.equals(other.m_value)) {
      return false;
    }
    return true;
  }

}
