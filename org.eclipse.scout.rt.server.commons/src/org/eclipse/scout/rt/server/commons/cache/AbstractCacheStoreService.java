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
package org.eclipse.scout.rt.server.commons.cache;

import java.io.IOException;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.IObjectSerializer;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 4.0.0
 */
public abstract class AbstractCacheStoreService extends AbstractService implements ICacheStoreService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCacheStoreService.class);

  private final IObjectSerializer m_objs;
  private Integer m_defaultExpirationTime = 60 * 60; // 60 Seconds * 60 Minutes = 3600 Seconds
  private final WeakHashMap<String, ICacheElement> m_node_cache;

  protected AbstractCacheStoreService() {
    m_node_cache = new WeakHashMap<String, ICacheElement>();
    m_objs = SerializationUtility.createObjectSerializer();
  }

  public void setExpiration(Integer expiration) {
    m_defaultExpirationTime = expiration;
  }

  public Integer getExpiration() {
    return m_defaultExpirationTime;
  }

  /**
   * returns the session id of the HTTP-Request. If no session id is set a new id will be generated and set.
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the session id
   */
  protected String getSessionId(HttpServletRequest req, HttpServletResponse res) {
    return SERVICES.getService(IClientIdentificationService.class).getClientId(req, res);
  }

  /**
   * Serializes objects
   * 
   * @param obj
   *          Object to serialize
   * @return bytestream
   */
  protected byte[] serialize(Object obj) {
    byte[] bytes = null;
    try {
      bytes = m_objs.serialize(obj);
    }
    catch (IOException e) {
      LOG.error("Error during serialization ", e);
    }
    return bytes;
  }

  /**
   * deserializes bytestreams
   * 
   * @param bytes
   *          bytestream
   * @return deserialized Object
   */
  protected Object deserialize(byte[] bytes) {
    Object obj = null;
    try {
      if (bytes != null) {
        obj = m_objs.deserialize(bytes, Object.class);
      }
    }
    catch (ClassNotFoundException e) {
      LOG.error("Error during deserializization ", e);

    }
    catch (IOException e) {
      LOG.error("Error during deserializization ", e);
    }
    return obj;
  }

  @Override
  public void setGlobalAttribute(String key, Object value) {
    m_node_cache.put(key, new CacheElement(value, m_defaultExpirationTime));
  }

  @Override
  public void setGlobalAttribute(String key, Object value, Integer expiration) {
    m_node_cache.put(key, new CacheElement(value, expiration));
  }

  @Override
  public Object getGlobalAttribute(String key) {
    ICacheElement e = m_node_cache.get(key);
    if (e.isActive()) {
      return e.getValue();
    }
    else {
      m_node_cache.remove(key);
      return null;
    }
  }

  @Override
  public Object getGlobalAttributeAndTouch(String key) {
    return getGlobalAttributeAndTouch(key, m_defaultExpirationTime);
  }

  @Override
  public Object getGlobalAttributeAndTouch(String key, Integer expiration) {
    ICacheElement e = m_node_cache.get(key);
    if (e.isActive()) {
      e.setExpiration(expiration);
      e.resetCreationTime();
      return e.getValue();
    }
    else {
      m_node_cache.remove(key);
      return null;
    }
  }

  @Override
  public void removeGlobalAttribute(String key) {
    m_node_cache.remove(key);
  }

  @Override
  public void touchGlobalAttribute(String key) {
    touchGlobalAttribute(key, m_defaultExpirationTime);
  }

  @Override
  public void touchGlobalAttribute(String key, Integer expiration) {
    ICacheElement e = m_node_cache.get(key);
    e.setExpiration(expiration);
    e.resetCreationTime();
  }

  protected class CacheElement implements ICacheElement {
    private final Object m_value;
    private long m_creationTime;
    private long m_expiration;

    /**
     * Element to be stored in the cache
     * 
     * @param value
     *          to be stored
     * @param expiration
     *          time in seconds
     */
    CacheElement(Object value, Integer expiration) {
      m_creationTime = System.currentTimeMillis();
      m_value = value;
      setExpiration(expiration);
    }

    @Override
    public boolean isActive() {
      return (m_creationTime + m_expiration < System.currentTimeMillis());
    }

    @Override
    public Object getValue() {
      return m_value;
    }

    @Override
    public void setExpiration(Integer expiration) {
      m_expiration = expiration * 1000l;
    }

    @Override
    public void resetCreationTime() {
      m_creationTime = System.currentTimeMillis();
    }
  }

}
