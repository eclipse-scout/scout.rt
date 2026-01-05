/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
 * In production, it makes heavy use of the max-age concept.
 * <p>
 * Make sure to call {@link #checkAndSetCacheHeaders(HttpServletRequest, HttpServletResponse, HttpCacheObject)} or {@link #disableCaching(HttpServletResponse)} in every request handler or servlet.
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
   * Contains the following values:
   * <ol>
   *   <li>no-store: A cache MUST NOT store this resource.</li>
   *   <li>
   *     no-transform: Some intermediaries transform content for various reasons. For example, some convert images to reduce transfer size.
   *     'no-transform' indicates that any intermediary (regardless of whether it implements a cache) shouldn't transform the response contents.
   *   </li>
   * </ol>
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control">MDN</a> for more details.
   */
  public static final String DISABLE_CACHE = "no-store, no-transform";
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
   * {@link RequestDispatcher#forward(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)}
   *
   * @param obj
   *     is the cache object that decides if cache is to be used or not, may be null to disable caching
   * @return true if the obj hasn't changed in the meantime. The {@link HttpServletResponse#SC_NOT_MODIFIED} response is
   * sent to the http response by this method and the caller should end its processing of this request.
   * <p>
   * false if the obj again needs to be fully returned, Etag, IfModifiedSince and MaxAge headers were set if
   * appropriate. If no caching is desired then the necessary headers were set.
   */
  public boolean checkAndSetCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheObject obj) {
    if (!UrlHints.isCacheHint(req)) {
      disableCaching(resp);
      return false;
    }

    if (obj == null || !obj.isCachingAllowed()) {
      disableCaching(resp);
      return false;
    }

    int maxAge = obj.getCacheMaxAge();
    if (maxAge > 0) {
      // "private"
      //   Only browsers may cache this resource.
      // "no-transform"
      //   Some intermediaries transform content for various reasons. For example, some convert images to reduce transfer size. In some cases, this is undesirable for the content provider.
      //   no-transform indicates that any intermediary (regardless of whether it implements a cache) shouldn't transform the response contents.
      // "max-age"
      //   A cache may use this resource for X seconds without checking with the server. s-maxage is ignored by private caches
      // Note: Because "must-revalidate" is not present, a cache MAY use a stale resource longer than max-age.
      resp.setHeader(CACHE_CONTROL, "private, no-transform, max-age=" + maxAge);
    }
    else {
      resp.setHeader(CACHE_CONTROL, "private, no-transform, max-age=0, must-revalidate");
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

  public void disableCaching(HttpServletResponse resp) {
    resp.setHeader(CACHE_CONTROL, DISABLE_CACHE);
  }

  protected boolean notModified(String ifNoneMatch, String etag) {
    return (ifNoneMatch != null && etag != null && ifNoneMatch.contains(etag));
  }

  // for purposes of comparison we add 999 to ifModifiedSince since the fidelity of the IMS header generally doesn't include milliseconds
  protected boolean notModifiedSince(long ifModifiedSince, long lastModified) {
    return (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + IF_MODIFIED_SINCE_FIDELITY));
  }
}
