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
package org.eclipse.scout.rt.server.commons.cache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.Order;

/**
 * This cache store service is for Scout Applications with "sticky sessions": A client always connects to the same node.
 * Or for applications with only one single server instance. The data stored does not to be serializable.
 *
 * @since 4.0.0
 */
@Order(5100)
public class StickySessionCacheService extends AbstractHttpSessionCacheService {

  @Override
  public void put(String key, Object value, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    if (value != null) {
      setEntryInternal(key, value, req, res, expiration);
    }
  }

  @Override
  public void remove(String key, HttpServletRequest req, HttpServletResponse res) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      session.removeAttribute(key);
    }
  }

  @Override
  public Object get(String key, HttpServletRequest req, HttpServletResponse res) {
    ICacheEntry e = getActiveEntry(key, req, res);
    return (e != null) ? e.getValue() : null;
  }

  @Override
  public void touch(String key, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    ICacheEntry e = getActiveEntry(key, req, res);
    if (e != null) {
      e.setExpiration(expiration);
      e.touch();
    }
  }

  protected ICacheEntry getActiveEntry(String key, HttpServletRequest req, HttpServletResponse res) {
    ICacheEntry e = getEntryInternal(key, req, res);
    if (e != null) {
      if (e.isActive()) {
        return e;
      }
      remove(key, req, res);
    }
    return null;
  }

  protected void setEntryInternal(String key, Object value, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    req.getSession(true).setAttribute(key, new CacheEntry<>(value, expiration));
  }

  protected ICacheEntry getEntryInternal(String key, HttpServletRequest req, HttpServletResponse res) {
    return (ICacheEntry) req.getSession(true).getAttribute(key);
  }
}
