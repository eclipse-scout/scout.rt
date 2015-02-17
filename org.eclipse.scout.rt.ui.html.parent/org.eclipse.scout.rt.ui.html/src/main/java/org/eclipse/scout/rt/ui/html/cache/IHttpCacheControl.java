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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Support for automatic caching of html resources.
 * <p>
 * In development mode the cache is disabled.
 * <p>
 * In production it makes heavey use of the max-age concept.
 */
public interface IHttpCacheControl {
  String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  int IF_MODIFIED_SINCE_FIDELITY = 999;
  String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  String ETAG = "ETag"; //$NON-NLS-1$

  /**
   * default value (in seconds) used for js and css in
   * {@link DefaultHttpCacheControl#getMaxAgeFor(HttpServletRequest, HttpServletResponse, HttpCacheInfo)}
   */
  int MAX_AGE_ONE_YEAR = 365 * 24 * 3600;
  /**
   * default value (in seconds) used for html, jpg, gif etc. in
   * {@link DefaultHttpCacheControl#getMaxAgeFor(HttpServletRequest, HttpServletResponse, HttpCacheInfo)}
   */
  int MAX_AGE_4_HOURS = 4 * 3600;

  void putCacheObject(HttpServletRequest req, HttpCacheObject o);

  HttpCacheObject getCacheObject(HttpServletRequest req, String cacheId);

  /**
   * @return the removed object or null if it was not cached
   */
  HttpCacheObject removeCacheObject(HttpServletRequest req, String cacheId);

  /**
   * Checks whether the file needs to be returned or not, depending on the request headers and file modification state.
   * Also writes cache headers (last modified and etag) if the file needs to be returned.
   * <p>
   * If info is null, then this method does nothing
   *
   * @return true if the file hasn't changed in the meantime. The {@link HttpServletResponse#SC_NOT_MODIFIED} reponse is
   *         then sent by this method and the caller should end its processing of this request.
   *         <p>
   *         false if the content of the file needs to be returned, Etag, IfModifiedSince and MaxAge headers were set if
   *         appropriate.
   */
  boolean checkAndUpdateCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheObject obj);

  /**
   * Disable cache for this resource
   */
  void disableCacheHeaders(HttpServletRequest req, HttpServletResponse resp);
}
