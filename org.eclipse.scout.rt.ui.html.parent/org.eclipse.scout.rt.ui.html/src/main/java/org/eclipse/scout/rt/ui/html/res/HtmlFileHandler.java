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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractRequestHandler;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;
import org.eclipse.scout.rt.ui.html.StreamUtility;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheInfo;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;

/**
 * Serve HTML files as a servlet resource using caches.
 * <p>
 * Process script tags in html files and enable automatic version and cache control handling
 * <p>
 * All occurrences of "-qualifier." are replaced by {@link IHttpCacheControl#getQualifierReplacement()}
 */
public class HtmlFileHandler extends AbstractRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HtmlFileHandler.class);
  private static final Pattern QUALIFIER_PATTERN = Pattern.compile("(?<=[-])qualifier(?=.)");
  private static final int MAX_AGE_4_HOURS = 4 * 3600;

  public HtmlFileHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    super(servlet, req, resp, pathInfo);
  }

  @Override
  public boolean handle() throws ServletException, IOException {
    HttpServletRequest req = getHttpServletRequest();
    HttpServletResponse resp = getHttpServletResponse();
    String pathInfo = getPathInfo();
    URL url = getServlet().getResourceLocator().getWebContentResource(getPathInfo());
    if (url == null) {
      return false;
    }
    LOG.info("processing html: " + pathInfo + " using " + url);

    //performance: did we already create a cached version?
    HttpCacheObject cacheObj = getServlet().getHttpCacheControl().getCacheObject(req, pathInfo);
    if (cacheObj == null) {
      byte[] content = StreamUtility.readResource(url);
      content = replaceQualifiers(content);
      cacheObj = new HttpCacheObject(pathInfo, content, System.currentTimeMillis());
      getServlet().getHttpCacheControl().putCacheObject(req, cacheObj);
    }

    //check cache state
    HttpCacheInfo info = new HttpCacheInfo(cacheObj.getContent().length, cacheObj.getLastModified(), MAX_AGE_4_HOURS);
    if (getServlet().getHttpCacheControl().checkAndUpdateCacheHeaders(req, resp, info)) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }

    resp.setContentType("text/html; charset=UTF-8");
    resp.setContentLength(cacheObj.getContent().length);
    resp.getOutputStream().write(cacheObj.getContent());
    return true;
  }

  /**
   * Replace all script "qualifer" texts in script and css tags by {@link IHttpCacheControl#getQualifierReplacement()}
   *
   * @throws UnsupportedEncodingException
   */
  protected byte[] replaceQualifiers(byte[] content) throws IOException {
    String s = new String(content, "UTF-8");
    s = QUALIFIER_PATTERN.matcher(s).replaceAll(getServlet().getHttpCacheControl().getQualifierReplacement());
    return s.getBytes("UTF-8");
  }

}
