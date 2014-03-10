/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.cache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.StringUtility;

/**
 * This Service is used for storing server side data in distributed systems with HTTP-Session synchronization by
 * servlet-container. The Objects will simply be stored in the HTTP-Session, the synchronization must be done by the
 * servlet container.
 * <p>
 * Serialization is done using IObjectSerializer, such that is working in an OSGi environment.
 * </p>
 * 
 * @author tsw
 * @since 4.0.0
 */
public class SerializedCacheService extends AbstractCacheStoreService {

  @Override
  public void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value) {
    setClientAttribute(req, key, value, getExpiration());
  }

  @Override
  public void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value, Integer expiration) {
    setClientAttribute(req, key, value, expiration);
  }

  private void setClientAttribute(HttpServletRequest req, String key, Object value, Integer expiration) {
    if (value != null) {
      req.getSession().setAttribute(key, StringUtility.bytesToHex(serialize(new CacheElement(value, expiration))));
    }
  }

  @Override
  public Object getClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    return getClientAttribute(req, key);
  }

  @Override
  public Object getClientAttributeAndTouch(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration) {
    return null;
  }

  private Object getClientAttribute(HttpServletRequest req, String key) {
    String hex = (String) req.getSession().getAttribute(key);
    if (hex != null) {
      CacheElement e = (CacheElement) deserialize(StringUtility.hexToBytes(hex));
      if (e != null && e.isActive()) {
        return e.getValue();
      }
      else if (e != null) {
        removeClientAttribute(req, key);
        return null;
      }
      else {
        return null;
      }
    }
    else {
      return null;
    }
  }

  @Override
  public void removeClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    removeClientAttribute(req, key);
  }

  private void removeClientAttribute(HttpServletRequest req, String key) {
    req.getSession().removeAttribute(key);
  }

  @Override
  public void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration) {
    touchClientAttribute(req, key, expiration);
  }

  private void touchClientAttribute(HttpServletRequest req, String key, Integer expiration) {
    String hex = (String) req.getSession().getAttribute(key);
    if (hex != null) {
      ICacheElement e = (ICacheElement) deserialize(StringUtility.hexToBytes(hex));
      if (e != null) {
        if (e.isActive()) {
          e.setExpiration(expiration);
          e.resetCreationTime();
        }
        else {
          removeClientAttribute(req, key);
        }
      }
    }
  }

  @Override
  public Object getClientAttributeAndTouch(HttpServletRequest req, HttpServletResponse res, String key) {
    return getClientAttributeAndTouch(req, res, key, getExpiration());
  }

  @Override
  public void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    touchClientAttribute(req, res, key, getExpiration());
  }

}
