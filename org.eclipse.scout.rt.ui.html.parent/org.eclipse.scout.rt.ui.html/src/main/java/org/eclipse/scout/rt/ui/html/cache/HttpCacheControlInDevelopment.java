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

/**
 * no caching at all
 */
public class HttpCacheControlInDevelopment implements IHttpCacheControl {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HttpCacheControlInDevelopment.class);

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
    resp.setHeader("cache-control", "private, max-age=0, no-cache, no-store, must-revalidate");
    return HttpServletResponse.SC_ACCEPTED;
  }

}
