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
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.ScoutAppHints;
import org.eclipse.scout.rt.ui.html.StreamUtility;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.service.AbstractService;

/**
 * This interceptor contributes to the {@link AbstractScoutAppServlet} as the default GET handler for
 * <p>
 * js, css, html, png, gif, jpg, woff
 */
@Priority(-10)
public class StaticResourceRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(StaticResourceRequestInterceptor.class);
  
  public static final String INDEX_HTML = "/index.html";
  public static final String MOBILE_INDEX_HTML = "/index-mobile.html";

  @Override
  public boolean interceptGet(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = resolvePathInfo(req);
    LOG.info("processing static resource: " + pathInfo + " using " + pathInfo);

    //lookup cache or load
    HttpCacheObject cacheObj = servlet.getHttpCacheControl().getCacheObject(req, pathInfo);
    if (cacheObj == null) {
      cacheObj = loadResource(servlet, req, pathInfo);
      //store in cache
      if (cacheObj != null) {
        servlet.getHttpCacheControl().putCacheObject(req, cacheObj);
      }
    }

    //check object existence
    if (cacheObj == null) {
      LOG.info("404_NOT_FOUND_GET: " + pathInfo);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    //cached in browser?
    if (servlet.getHttpCacheControl().checkAndUpdateCacheHeaders(req, resp, cacheObj.toCacheInfo())) {
      return true;
    }

    //return content
    String contentType = detectContentType(servlet, pathInfo);
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    else {
      LOG.warn("Could not determine content type of: " + pathInfo);
    }
    resp.setContentLength(cacheObj.getContent().length);
    resp.getOutputStream().write(cacheObj.getContent());
    return true;
  }

  @Override
  public boolean interceptPost(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  protected String resolvePathInfo(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return null;
    }
    if ("/".equals(pathInfo)) {
      pathInfo = resolveIndexHtml(req);
    }
    return pathInfo;
  }

  protected String resolveIndexHtml(HttpServletRequest request) {
    BrowserInfo browserInfo = new BrowserInfoBuilder().createBrowserInfo(request);
    if (browserInfo.isMobile()) {
      return MOBILE_INDEX_HTML;
    }
    return INDEX_HTML;
  }

  protected String detectContentType(AbstractScoutAppServlet servlet, String path) {
    int lastSlash = path.lastIndexOf('/');
    String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    //Prefer mime type mapping from container
    String contentType = servlet.getServletContext().getMimeType(fileName);
    if (contentType != null) {
      return contentType;
    }
    int lastDot = path.lastIndexOf('.');
    String fileExtension = lastDot >= 0 ? path.substring(lastDot + 1) : path;
    return FileUtility.getContentTypeForExtension(fileExtension);
  }

  /**
   * create new resource
   */
  protected HttpCacheObject loadResource(AbstractScoutAppServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    if ((pathInfo.endsWith(".js") || pathInfo.endsWith(".css"))) {
      return loadScriptFile(servlet, req, pathInfo);
    }
    if (pathInfo.endsWith(".html")) {
      return loadHtmlFile(servlet, req, pathInfo);
    }
    return loadBinaryFile(servlet, req, pathInfo);
  }

  /**
   * js, css
   */
  protected HttpCacheObject loadScriptFile(AbstractScoutAppServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    ScriptFileBuilder builder = new ScriptFileBuilder(servlet.getResourceLocator());
    builder.setMinifyEnabled(ScoutAppHints.isMinifyHint(req));
    return builder.buildScript(pathInfo);
  }

  /**
   * html
   */
  protected HttpCacheObject loadHtmlFile(AbstractScoutAppServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = servlet.getResourceLocator().getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    byte[] content = StreamUtility.readResource(url);
    content = replaceHtmlScriptTags(servlet, req, content);
    return new HttpCacheObject(pathInfo, content, System.currentTimeMillis());
  }

  /**
   * png, jpg, woff, pdf, docx
   */
  protected HttpCacheObject loadBinaryFile(AbstractScoutAppServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = servlet.getResourceLocator().getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    byte[] content = StreamUtility.readResource(url);
    URLConnection uc = url.openConnection();
    return new HttpCacheObject(pathInfo, content, uc.getLastModified());
  }

  /**
   * Process all js and css script tags that contain the marker text "fingerprint". The marker text is replaced by the
   * effective files {@link HttpCacheObject#getFingerprint()} in hex format
   */
  protected byte[] replaceHtmlScriptTags(AbstractScoutAppServlet servlet, HttpServletRequest req, byte[] content) throws IOException, ServletException {
    String oldHtml = new String(content, "UTF-8");
    Matcher m = ScriptFileBuilder.SCRIPT_URL_PATTERN.matcher(oldHtml);
    StringBuilder buf = new StringBuilder();
    int lastEnd = 0;
    int replaceCount = 0;
    while (m.find()) {
      buf.append(oldHtml.substring(lastEnd, m.start()));
      if ("fingerprint".equals(m.group(4))) {
        replaceCount++;
        HttpCacheObject obj = loadScriptFile(servlet, req, m.group());
        if (obj == null) {
          LOG.warn("Failed to locate resource referenced in html file '" + req.getPathInfo() + "': " + m.group());
        }
        String fingerprint = (obj != null ? Long.toHexString(obj.getFingerprint()) : m.group(4));
        buf.append(m.group(1));
        buf.append(m.group(2));
        buf.append("-");
        buf.append(m.group(3));
        buf.append("-");
        buf.append(fingerprint);
        buf.append(".min.");
        buf.append(m.group(5));
      }
      else {
        buf.append(m.group());
      }
      //next
      lastEnd = m.end();
    }
    if (replaceCount == 0) {
      return content;
    }
    buf.append(oldHtml.substring(lastEnd));
    String newHtml = buf.toString();
    if (LOG.isDebugEnabled()) {
      LOG.debug("process html script tags:\nINPUT\n" + oldHtml + "\n\nOUTPUT\n" + newHtml);

    }
    return newHtml.getBytes("UTF-8");
  }
}
