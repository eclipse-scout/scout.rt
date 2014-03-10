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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

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
public class SerializedCacheStoreService extends AbstractCacheStoreService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SerializedCacheStoreService.class);

  @Override
  public void setClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Object value, Integer expiration) {
    if (value != null) {
      try {
        req.getSession().setAttribute(key, serializedString(new CacheElement(value, expiration)));
      }
      catch (ProcessingException e) {
        LOG.error("Error during serialization", e);
      }
    }
  }

  @Override
  public Object getClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    ICacheElement e = deserializeCacheElement((String) req.getSession().getAttribute(key));
    if (e != null && e.isActive()) {
      return e.getValue();
    }
    else if (e != null) {
      removeClientAttribute(req, res, key);
    }
    return null;
  }

  @Override
  public void removeClientAttribute(HttpServletRequest req, HttpServletResponse res, String key) {
    req.getSession().removeAttribute(key);
  }

  @Override
  public void touchClientAttribute(HttpServletRequest req, HttpServletResponse res, String key, Integer expiration) {
    ICacheElement e = deserializeCacheElement((String) req.getSession().getAttribute(key));
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
