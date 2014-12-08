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
package org.eclipse.scout.rt.ui.html.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.ScoutAppHints;

public abstract class AbstractHttpCacheControl implements IHttpCacheControl {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractHttpCacheControl.class);

  /**
   * This cache is servlet-wide (all sessions)
   */
  private Map<String, HttpCacheObject> m_cache = Collections.synchronizedMap(new HashMap<String, HttpCacheObject>());

  protected AbstractHttpCacheControl() {
  }

  @Override
  public void putCacheObject(HttpServletRequest req, HttpCacheObject o) {
    if (!ScoutAppHints.isCacheHint(req)) {
      return;
    }
    m_cache.put(o.getPathInfo(), o);
  }

  @Override
  public HttpCacheObject getCacheObject(HttpServletRequest req, String pathInfo) {
    if (!ScoutAppHints.isCacheHint(req)) {
      return null;
    }
    return m_cache.get(pathInfo);
  }

  @Override
  public HttpCacheObject removeCacheObject(HttpServletRequest req, String pathInfo) {
    if (!ScoutAppHints.isCacheHint(req)) {
      return null;
    }
    return m_cache.remove(pathInfo);
  }

  @Override
  public boolean checkAndUpdateCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheInfo info) {
    if (!ScoutAppHints.isCacheHint(req)) {
      disableCacheHeaders(req, resp, info);
      return false;
    }
    int maxAge = info.getPreferredCacheMaxAge();
    resp.setHeader("cache-control", "public, max-age=" + maxAge + ", s-maxage=" + maxAge);

    String etag = info.createETag();

    // Check for cache revalidation.
    // We should prefer ETag validation as the guarantees are stronger and all
    // HTTP 1.1 clients should be using it
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (notModified(ifNoneMatch, etag)) {
      LOG.info("http-cache (etag): " + req.getPathInfo());
      return true;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the fidelity of the IMS header generally doesn't include milli-seconds
      if (notModifiedSince(ifModifiedSince, info.getLastModified())) {
        LOG.info("http-cache (ifModifiedSince): " + req.getPathInfo());
        return true;
      }
    }

    if (info.getLastModified() > 0) {
      resp.setDateHeader(LAST_MODIFIED, info.getLastModified());
    }
    if (etag != null) {
      resp.setHeader(ETAG, etag);
    }

    return false;
  }

  @Override
  public void disableCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheInfo info) {
    resp.setHeader("cache-control", "private, max-age=0, no-cache, no-store, must-revalidate");
  }

  protected boolean notModified(String ifNoneMatch, String etag) {
    return (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1);
  }

  protected boolean notModifiedSince(long ifModifiedSince, long lastModified) {
    return (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + IF_MODIFIED_SINCE_FIDELITY));
  }

}
