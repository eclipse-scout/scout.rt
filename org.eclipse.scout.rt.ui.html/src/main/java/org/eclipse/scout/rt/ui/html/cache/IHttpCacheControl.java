/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.cache;

import java.io.Serializable;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.resource.BinaryResources;

/**
 * Support for automatic caching of html resources.
 * <p>
 * In development mode the cache is disabled.
 * <p>
 * In production it makes heavy use of the max-age concept.
 */
@ApplicationScoped
public interface IHttpCacheControl extends Serializable {
  String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  int IF_MODIFIED_SINCE_FIDELITY = 999;
  String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  String ETAG = "ETag"; //$NON-NLS-1$
  String CACHE_CONTROL = "Cache-Control"; //$NON-NLS-1$

  /**
   * default value (in seconds) used for js and css
   */
  int MAX_AGE_ONE_YEAR = 365 * 24 * 3600;

  /**
   * default value (in seconds) used for html, jpg, gif etc.
   */
  int MAX_AGE_4_HOURS = 4 * 3600;

  /**
   * value used to disable cache-control, only e-tag and if-modified-since may further be used
   */
  int MAX_AGE_NONE = 0;

  /**
   * Put an object into the internal servlet cache if {@link HttpCacheObject#isCachingAllowed()} is true.
   *
   * @param req
   * @param obj
   * @return true if the object was cached or null if it was not cached
   */
  boolean putCacheObject(HttpServletRequest req, HttpCacheObject obj);

  /**
   * Remove an object from the internal servlet cache.
   *
   * @param req
   * @param cacheKey
   * @return the object from the internal servlet cache or null
   */
  HttpCacheObject getCacheObject(HttpServletRequest req, HttpCacheKey cacheKey);

  /**
   * @return the removed object or null if it was not cached
   */
  HttpCacheObject removeCacheObject(HttpServletRequest req, HttpCacheKey cacheKey);

  /**
   * Checks whether a cached response (304) can be returned or not, depending on the request headers and
   * {@link BinaryResources}.
   * <p>
   * Writes cache headers (last modified and etag) if the obj can safely be returned as cached object.
   * <p>
   * Writes disbaled cache headers if the obj is null or cannot safely be returned resp. should not be cached at all.
   * <p>
   * Does nothing if this request is a forward such as
   * {@link RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
   *
   * @param req
   * @param resp
   * @param pathInfo
   *          optional resolved pathInfo. If null then {@link HttpServletRequest#getPathInfo()} is used as default.
   * @param obj
   *          is the cache object that decides if cache is to be used or not, may be null to disable caching
   * @return true if the obj hasn't changed in the meantime. The {@link HttpServletResponse#SC_NOT_MODIFIED} response is
   *         sent to the http response by this method and the caller should end its processing of this request.
   *         <p>
   *         false if the obj again needs to be fully returned, Etag, IfModifiedSince and MaxAge headers were set if
   *         appropriate. If no caching is desired then the disable headers were set.
   */
  boolean checkAndSetCacheHeaders(HttpServletRequest req, HttpServletResponse resp, String pathInfo, HttpCacheObject obj);
}
