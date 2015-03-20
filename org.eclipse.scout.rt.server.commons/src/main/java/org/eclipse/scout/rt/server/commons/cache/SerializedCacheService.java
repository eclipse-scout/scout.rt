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
public class SerializedCacheService extends StickySessionCacheService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SerializedCacheService.class);

  @Override
  protected void setEntryInternal(String key, Object value, HttpServletRequest req, HttpServletResponse res, Long expiration) {
    try {
      req.getSession(true).setAttribute(key, serializedString(new CacheEntry<Object>(value, expiration)));
    }
    catch (ProcessingException e) {
      LOG.error("Error during serialization", e);
    }
  }

  @Override
  protected ICacheEntry getEntryInternal(String key, HttpServletRequest req, HttpServletResponse res) {
    return (ICacheEntry) deserialize((String) req.getSession(true).getAttribute(key));
  }

}
