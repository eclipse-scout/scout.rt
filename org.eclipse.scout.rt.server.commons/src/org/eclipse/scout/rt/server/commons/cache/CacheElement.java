package org.eclipse.scout.rt.server.commons.cache;

import java.io.Serializable;

class CacheElement implements ICacheElement, Serializable {
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

}
