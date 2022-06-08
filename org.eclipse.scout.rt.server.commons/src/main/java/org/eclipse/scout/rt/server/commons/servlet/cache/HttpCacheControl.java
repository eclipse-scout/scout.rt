/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for caching of HTML resources via HTTP headers.
 * <p>
 * In development mode the cache is disabled.
 * <p>
 * In production it makes heavy use of the max-age concept.
 * <p>
 * Make sure to call {@link #checkAndSetCacheHeaders(HttpServletRequest, HttpServletResponse, HttpCacheObject)} in every
 * servlet.
 */
@ApplicationScoped
public class HttpCacheControl {

  private static final Logger LOG = LoggerFactory.getLogger(HttpCacheControl.class);

  public static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  public static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  public static final int IF_MODIFIED_SINCE_FIDELITY = 999;
  public static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  public static final String ETAG = "ETag"; //$NON-NLS-1$
  public static final String CACHE_CONTROL = "Cache-Control"; //$NON-NLS-1$
  /**
   * default value (in seconds) used for js and css
   */
  public static final int MAX_AGE_ONE_YEAR = 365 * 24 * 3600;
  /**
   * default value (in seconds) used for html, jpg, gif etc.
   */
  public static final int MAX_AGE_4_HOURS = 4 * 3600;
  /**
   * value used to disable cache-control, only e-tag and if-modified-since may further be used
   */
  public static final int MAX_AGE_NONE = 0;

  /**
   * Checks whether a cached response (304) can be returned or not, depending on the request headers and
   * {@link BinaryResources}.
   * <p>
   * Writes cache headers (last modified and etag) if the obj can safely be returned as cached object.
   * <p>
   * Writes disabled cache headers if the obj is null or cannot safely be returned resp. should not be cached at all.
   * <p>
   * Does nothing if this request is a forward such as
   * {@link RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
   *
   * @param obj
   *          is the cache object that decides if cache is to be used or not, may be null to disable caching
   * @return true if the obj hasn't changed in the meantime. The {@link HttpServletResponse#SC_NOT_MODIFIED} response is
   *         sent to the http response by this method and the caller should end its processing of this request.
   *         <p>
   *         false if the obj again needs to be fully returned, Etag, IfModifiedSince and MaxAge headers were set if
   *         appropriate. If no caching is desired then the necessary headers were set.
   */
  public boolean checkAndSetCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheObject obj) {
    if (!UrlHints.isCacheHint(req)) {
      disableCaching(req, resp);
      return false;
    }

    if (obj == null || !obj.isCachingAllowed()) {
      disableCaching(req, resp);
      return false;
    }

    int maxAge = obj.getCacheMaxAge();
    if (maxAge > 0) {
      // "private"
      //   Only browsers may cache this resource.
      // "max-age"
      //   A cache may use this resource for X seconds without checking with the server. s-maxage
      //   is basically the same, but for proxies (s = shared). This overrides any default value
      //   the proxy may use internally.
      // Note: Because "must-revalidate" is not present, a cache MAY use a stale resource longer than max-age.
      resp.setHeader(CACHE_CONTROL, "private, max-age=" + maxAge + ", s-maxage=" + maxAge);
    }
    else {
      // "private"
      //   Only browsers may cache this resource.
      // "must-revalidate"
      //   A cache HAS TO check with the server before using stale resources.
      // "max-age=0"
      //   A resource will become stale immediately (after 0 seconds).
      // Note: "max-age=0, must-revalidate" would be the same as "no-cache"
      resp.setHeader(CACHE_CONTROL, "private, max-age=0, must-revalidate");
    }

    String etag = obj.createETag();
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    boolean clientSentEtag = (ifNoneMatch != null);

    // Check If-None-Match (Etag)
    if (clientSentEtag) {
      if (notModified(ifNoneMatch, etag)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Use http cached object (If-None-Match/Etag): {}", req.getPathInfo());
        }
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return true;
      }
      // When the Etag comparison fails (i.e. file was modified), we must _not_ check for If-Modified-Since!
    }
    // Check If-Modified-Since
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      if (notModifiedSince(ifModifiedSince, obj.getResource().getLastModified())) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Use http cached object (If-Modified-Since): {}", req.getPathInfo());
        }
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

  protected void disableCaching(HttpServletRequest req, HttpServletResponse resp) {
    // "private"
    //   Only browsers may cache this resource.
    // "no-store"
    //   A cache MUST NOT store this resource.
    // "no-cache"
    //   A cache MUST NOT re-use this resource for subsequent requests.
    // "max-age=0"
    //   Should not be necessary here, but because some browser apparently imply a
    //   short caching time with "no-cache" (http://stackoverflow.com/a/19938619),
    //   we explicitly set it to 0.
    resp.setHeader(CACHE_CONTROL, "private, no-store, no-cache, max-age=0");
  }

  protected boolean notModified(String ifNoneMatch, String etag) {
    return (ifNoneMatch != null && etag != null && ifNoneMatch.contains(etag));
  }

  // for purposes of comparison we add 999 to ifModifiedSince since the fidelity of the IMS header generally doesn't include milliseconds
  protected boolean notModifiedSince(long ifModifiedSince, long lastModified) {
    return (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + IF_MODIFIED_SINCE_FIDELITY));
  }
}
