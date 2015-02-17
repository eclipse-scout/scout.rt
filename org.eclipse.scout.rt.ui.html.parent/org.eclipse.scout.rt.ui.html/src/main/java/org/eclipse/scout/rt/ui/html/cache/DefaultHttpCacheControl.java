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
import org.eclipse.scout.rt.ui.html.UiHints;

public class DefaultHttpCacheControl implements IHttpCacheControl {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultHttpCacheControl.class);

  /**
   * This cache is servlet-wide (all sessions)
   */
  private Map<String, HttpCacheObject> m_cache = Collections.synchronizedMap(new HashMap<String, HttpCacheObject>());

  public DefaultHttpCacheControl() {
  }

  @Override
  public void putCacheObject(HttpServletRequest req, HttpCacheObject obj) {
    if (!UiHints.isCacheHint(req)) {
      return;
    }
    if (!obj.isCachingAllowed()) {
      return;
    }
    m_cache.put(obj.getCacheId(), obj);
  }

  @Override
  public HttpCacheObject getCacheObject(HttpServletRequest req, String cacheId) {
    if (!UiHints.isCacheHint(req)) {
      return null;
    }
    return m_cache.get(cacheId);
  }

  @Override
  public HttpCacheObject removeCacheObject(HttpServletRequest req, String cacheId) {
    if (!UiHints.isCacheHint(req)) {
      return null;
    }
    return m_cache.remove(cacheId);
  }

  @Override
  public boolean checkAndUpdateCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheObject obj) {
    if (!UiHints.isCacheHint(req)) {
      disableCacheHeaders(req, resp);
      return false;
    }
    if (!obj.isCachingAllowed()) {
      return false;
    }
    int maxAge = getMaxAgeFor(req, resp, obj);
    if (maxAge > 0) {
      resp.setHeader("cache-control", "private, max-age=" + maxAge + ", s-maxage=" + maxAge);
    }
    else {
      resp.setHeader("cache-control", "private, max-age=0, must-revalidate");
    }

    String etag = obj.createETag();
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (notModified(ifNoneMatch, etag)) {
      LOG.info("use http cached object (etag): " + req.getPathInfo());
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the fidelity of the IMS header generally doesn't include milli-seconds
      if (notModifiedSince(ifModifiedSince, obj.getResource().getLastModified())) {
        LOG.info("use http cached object (ifModifiedSince): " + req.getPathInfo());
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return true;
      }
    }

    if (obj.getResource().getLastModified() > 0) {
      resp.setDateHeader(LAST_MODIFIED, obj.getResource().getLastModified());
    }
    if (etag != null) {
      resp.setHeader(ETAG, etag);
    }

    return false;
  }

  /**
   * @return the preferred max age or 0/negative in oder to omit a max-age header
   */
  protected int getMaxAgeFor(HttpServletRequest req, HttpServletResponse resp, HttpCacheObject obj) {
    if (obj.getCacheMaxAge() > 0) {
      return obj.getCacheMaxAge();
    }
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return MAX_AGE_4_HOURS;
    }
    if (pathInfo.endsWith(".js") || pathInfo.endsWith(".css")) {
      return MAX_AGE_ONE_YEAR;
    }
    return MAX_AGE_4_HOURS;
  }

  @Override
  public void disableCacheHeaders(HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader("cache-control", "private, max-age=0, no-cache, no-store, must-revalidate");
  }

  protected boolean notModified(String ifNoneMatch, String etag) {
    return (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1);
  }

  protected boolean notModifiedSince(long ifModifiedSince, long lastModified) {
    return (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + IF_MODIFIED_SINCE_FIDELITY));
  }

}
