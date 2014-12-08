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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractRequestHandler;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;
import org.eclipse.scout.rt.ui.html.ScoutAppHints;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheInfo;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;

/**
 * Serve CSS and JS files as a servlet resource using caches.
 * <p>
 * see {@link ScriptBuilder}
 */
public class ScriptFileHandler extends AbstractRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScriptFileHandler.class);
  private static final int MAX_AGE_30_DAYS = 30 * 24 * 3600;

  public ScriptFileHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    super(servlet, req, resp, pathInfo);
  }

  @Override
  public boolean handle() throws ServletException, IOException {
    HttpServletRequest req = getHttpServletRequest();
    HttpServletResponse resp = getHttpServletResponse();
    String pathInfo = getPathInfo();
    LOG.info("processing script: " + pathInfo);

    //performance: did we already create a cached version?
    HttpCacheObject cacheObj = getServlet().getHttpCacheControl().getCacheObject(req, pathInfo);
    if (cacheObj == null) {
      ScriptBuilder builder = createScriptBuilder();
      builder.setMinifyEnabled(ScoutAppHints.isMinifyHint(req));
      cacheObj = builder.buildScript(pathInfo);
      getServlet().getHttpCacheControl().putCacheObject(req, cacheObj);
    }

    //check cache state
    HttpCacheInfo info = new HttpCacheInfo(cacheObj.getContent().length, cacheObj.getLastModified(), MAX_AGE_30_DAYS);
    if (getServlet().getHttpCacheControl().checkAndUpdateCacheHeaders(req, resp, info)) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }

    if (pathInfo.endsWith(".js")) {
      resp.setContentType("application/javascript");
    }
    else {
      resp.setContentType("text/css");
    }
    resp.setContentLength(cacheObj.getContent().length);
    resp.getOutputStream().write(cacheObj.getContent());
    return true;
  }

  protected ScriptBuilder createScriptBuilder() {
    return new ScriptBuilder(getServlet().getResourceLocator());
  }

}
