package org.eclipse.scout.rt.server.commons.cache;

import java.io.Serializable;

final class CacheElement implements ICacheElement, Serializable {
  private static final long serialVersionUID = 1L;
  private final Object m_value;
  private long m_creationTime;
  private long m_expiration;

  CacheElement(Object value, Integer expiration, long creationTime) {
    m_creationTime = creationTime;
    m_value = value;
    setExpiration(expiration);
  }

  /**
   * Element to be stored in the cache
   * 
   * @param value
   *          to be stored
   * @param expiration
   *          time in seconds
   */
  CacheElement(Object value, Integer expiration) {
    this(value, expiration, System.currentTimeMillis());
  }

  @Override
  public boolean isActive() {
    return (m_creationTime + m_expiration > System.currentTimeMillis());
  }

  @Override
  public Object getValue() {
    return m_value;
  }

  @Override
  public void setExpiration(Integer expiration) {
    m_expiration = expiration * 1000l;
  }

  public long getCreationTime() {
    return m_creationTime;
  }

  @Override
  public void resetCreationTime() {
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
    CacheElement other = (CacheElement) obj;
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
