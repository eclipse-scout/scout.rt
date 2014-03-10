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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the cache store service is for Scout Applications running on a single server instance. The data stored does
 * not to be serializable.
 */
public class SingleNodeCacheStoreService extends AbstractCacheStoreService {

  @Override
  public void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value, Integer expiration) {
    req.getSession().setAttribute(key, new CacheElement(value, expiration));
  }

  @Override
  public void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value) {
    req.getSession().setAttribute(key, new CacheElement(value, getExpiration()));
  }

  @Override
  public Object getClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    return getClientAttribute(req, key);
  }

  private Object getClientAttribute(HttpServletRequest req, String key) {
    CacheElement e = (CacheElement) req.getSession().getAttribute(key);
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

  @Override
  public void removeClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    removeClientAttribute(req, key);
  }

  private void removeClientAttribute(HttpServletRequest req, String key) {
    req.getSession().removeAttribute(key);
  }

  @Override
  public Object getClientAttributeAndTouch(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration) {
    Object o = getClientAttribute(req, key);
    touchClientAttribute(req, key, expiration);
    return o;
  }

  @Override
  public void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration) {
    touchClientAttribute(req, key, expiration);
  }

  private void touchClientAttribute(HttpServletRequest req, String key, Integer expiration) {
    ICacheElement e = (ICacheElement) req.getSession().getAttribute(key);
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

  @Override
  public Object getClientAttributeAndTouch(HttpServletRequest req, HttpServletResponse res, String key) {
    return getClientAttributeAndTouch(req, res, key, getExpiration());
  }

  @Override
  public void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    touchClientAttribute(req, res, key, getExpiration());
  }

}
