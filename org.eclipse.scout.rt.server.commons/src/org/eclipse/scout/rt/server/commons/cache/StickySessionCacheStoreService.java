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
 * This cache store service is for Scout Applications with "sticky sessions": A client always connects to the same node.
 * Or for applications with only one single server instance. The data stored does not to be serializable.
 * 
 * @since 4.0.0
 */
public class StickySessionCacheStoreService extends AbstractCacheStoreService {

  @Override
  public void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value, Integer expiration) {
    req.getSession().setAttribute(key, new CacheElement(value, expiration));
  }

  @Override
  public Object getClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    ICacheElement e = getCacheElement(req, key);
    if (e != null && e.isActive()) {
      return e.getValue();
    }
    else if (e != null) {
      removeClientAttribute(req, res, key);
      return null;
    }
    else {
      return null;
    }
  }

  private ICacheElement getCacheElement(HttpServletRequest req, String key) {
    return (ICacheElement) req.getSession().getAttribute(key);
  }

  @Override
  public void removeClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    req.getSession().removeAttribute(key);
  }

  @Override
  public void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration) {
    ICacheElement e = getCacheElement(req, key);
    if (e != null) {
      if (e.isActive()) {
        e.setExpiration(expiration);
        e.resetCreationTime();
      }
      else {
        removeClientAttribute(req, res, key);
      }
    }
  }
}
