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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class HttpCacheControlInProduction implements IHttpCacheControl {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HttpCacheControlInProduction.class);

  @Override
  public void putCacheInfo(HttpCacheInfo info) {
  }

  @Override
  public HttpCacheInfo getCacheInfo(String requestPath) {
    return null;
  }

  @Override
  public HttpCacheInfo removeCacheInfo(String requestPath) {
    return null;
  }

  @Override
  public int processCacheHeaders(HttpServletRequest req, HttpServletResponse resp, HttpCacheInfo info) {
    resp.setHeader("cache-control", "public, max-age=240, s-maxage=240");//FIXME imo depending on resource type some other times

    String etag = info.createETag();

    // Check for cache revalidation.
    // We should prefer ETag validation as the guarantees are stronger and all
    // HTTP 1.1 clients should be using it
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (notModified(ifNoneMatch, etag)) {
      return HttpServletResponse.SC_NOT_MODIFIED;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the fidelity of the IMS header generally doesn't include milli-seconds
      if (notModifiedSince(ifModifiedSince, info.getLastModified())) {
        return HttpServletResponse.SC_NOT_MODIFIED;
      }
    }

    // File needs to be returned regularly, write cache headers
    if (info.getLastModified() > 0) {
      resp.setDateHeader(LAST_MODIFIED, info.getLastModified());
    }
    if (etag != null) {
      resp.setHeader(ETAG, etag);
    }

    return HttpServletResponse.SC_ACCEPTED;
  }

  protected boolean notModified(String ifNoneMatch, String etag) {
    return (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1);
  }

  protected boolean notModifiedSince(long ifModifiedSince, long lastModified) {
    return (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + IF_MODIFIED_SINCE_FIDELITY));
  }

}
