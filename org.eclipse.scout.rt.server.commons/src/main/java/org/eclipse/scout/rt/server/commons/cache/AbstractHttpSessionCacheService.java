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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for caching server side data dependent on request/response.
 * <p>
 * Cached values may expire. Expired Values are removed from the cache.
 * </p>
 *
 * @since 4.0.0
 */
public abstract class AbstractHttpSessionCacheService implements IHttpSessionCacheService {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpSessionCacheService.class);

  private final IObjectSerializer m_objs;

  //expiration time in milliseconds
  private final AtomicLong m_defaultExpirationTime = new AtomicLong(Long.parseLong(ObjectUtility.nvl(ConfigUtility.getProperty("org.eclipse.scout.rt.server.commons.cache.AbstractHttpSessionCacheService#expiration"), "3600000")));

  protected AbstractHttpSessionCacheService() {
    m_objs = SerializationUtility.createObjectSerializer();
  }

  /**
   * set the expiration in milliseconds
   */
  public void setExpiration(Long expiration) {
    m_defaultExpirationTime.set(expiration);
  }

  @Override
  public Long getExpiration() {
    return m_defaultExpirationTime.get();
  }

  @Override
  public void put(String key, Object value, HttpServletRequest req, HttpServletResponse res) {
    put(key, value, req, res, getExpiration());
  }

  @Override
  public Object getAndTouch(String key, HttpServletRequest req, HttpServletResponse res) {
    return getAndTouch(key, req, res, getExpiration());
  }

  @Override
  public void touch(String key, HttpServletRequest req, HttpServletResponse res) {
    touch(key, req, res, getExpiration());
  }

  @Override
  public Object getAndTouch(String key, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    Object v = get(key, req, res);
    touch(key, req, res, expiration);
    return v;
  }

  /**
   * Serializes cache element
   *
   * @param e
   *          Object to serialize
   * @return {@link String}
   */
  protected String serializedString(Object e) {
    try {
      byte[] bytes = m_objs.serialize(e);
      return HexUtility.encode(bytes);
    }
    catch (IOException ex) {
      throw new ProcessingException("Error during Serialization ", ex);
    }
  }

  /**
   * deserializes bytestreams
   *
   * @param bytes
   *          bytestream
   * @return deserialized Object
   */
  protected Object deserialize(String s) {
    if (s == null) {
      return null;
    }
    try {
      byte[] bytes = HexUtility.decode(s);
      if (bytes != null) {
        return m_objs.deserialize(bytes, Object.class);
      }
    }
    catch (ClassNotFoundException e) {
      LOG.error("ClassNotFoundException error during deserializization ", e);
    }
    catch (IOException e) {
      LOG.error("Error during deserializization ", e);
    }
    return null;
  }

}
