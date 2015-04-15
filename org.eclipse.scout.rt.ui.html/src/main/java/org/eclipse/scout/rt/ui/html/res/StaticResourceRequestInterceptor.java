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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.shared.data.basic.BinaryResource;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiHints;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonUtility;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;
import org.eclipse.scout.rt.ui.html.script.ScriptOutput;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * This interceptor contributes to the {@link UiServlet} as the default GET handler for
 * <p>
 * js, css, html, png, gif, jpg, woff, json
 */
@Priority(-10)
public class StaticResourceRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(StaticResourceRequestInterceptor.class);

  public static final String INDEX_HTML = "/index.html";
  public static final String MOBILE_INDEX_HTML = "/index-mobile.html";

  private static final String UTF_8 = "UTF-8";
  private static final Set<String> DOWNLOAD_CONTENT_TYPES = new HashSet<String>(Arrays.asList(new String[]{
      "application/octet-stream",
      "application/octet-stream",
      "application/pdf",
      "application/mspowerpoint",
      "application/mspowerpoint",
      "application/mspowerpoint",
      "application/mspowerpoint",
      "application/vnd.ms-excel",
      "application/vnd.ms-excel",
      "application/vnd.ms-excel",
      "application/vnd.ms-excel",
      "application/vnd.ms-excel",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
      "application/vnd.openxmlformats-officedocument.presentationml.template",
      "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.openxmlformats-officedocument.presentationml.slide",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
      "application/zip",
  }));
  private static final Pattern PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH = Pattern.compile("^/dynamic/([^/]*)/([^/]*)/(.*)$");

  private ScriptProcessor m_scriptProcessor;

  @Override
  public boolean interceptGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = resolvePathInfo(req);
    LOG.debug("processing static resource: " + pathInfo);

    // lookup cache or load
    IHttpCacheControl httpCacheControl = servlet.getHttpCacheControl();
    HttpCacheObject cacheObj = httpCacheControl.getCacheObject(req, pathInfo);
    if (cacheObj == null) {
      cacheObj = loadResource(servlet, req, pathInfo);
      // store in cache
      if (cacheObj != null) {
        httpCacheControl.putCacheObject(req, cacheObj);
      }
    }

    // check object existence
    if (cacheObj == null) {
      LOG.info("404_NOT_FOUND_GET: " + pathInfo);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    String contentType = cacheObj.getResource().getContentType();
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    else {
      LOG.warn("Could not determine content type of: " + pathInfo);
    }
    resp.setContentLength(cacheObj.getResource().getContentLength());

    // cached in browser? -> returns 304 if the resource has not been modified
    // Important: Check is only done if the request still processes the requested resource and hasn't been forwarded to another one (using req.getRequestDispatcher().forward)
    String originalPathInfo = (String) req.getAttribute("javax.servlet.forward.path_info");
    if (originalPathInfo == null || pathInfo.equals(originalPathInfo)) {
      if (httpCacheControl.checkAndUpdateCacheHeaders(req, resp, cacheObj)) {
        return true;
      }
    }

    if (isDownload(cacheObj.getResource())) {
      resp.setHeader("Content-Disposition", "attachment");
    }

    if (!"HEAD".equals(req.getMethod())) {
      resp.getOutputStream().write(cacheObj.getResource().getContent());
    }
    return true;
  }

  @Override
  public boolean interceptPost(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  protected synchronized ScriptProcessor getSharedScriptProcessor() {
    if (m_scriptProcessor == null) {
      m_scriptProcessor = new ScriptProcessor();
    }
    return m_scriptProcessor;
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
    BrowserInfo browserInfo = BrowserInfo.createFrom(request);
    if (browserInfo.isMobile()) {
      return MOBILE_INDEX_HTML;
    }
    return INDEX_HTML;
  }

  /**
   * Loads a resource with an appropriate method, based on the URL
   */
  protected HttpCacheObject loadResource(UiServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    if (pathInfo.matches("^/icon/.*")) {
      return loadIcon(servlet, req, pathInfo);
    }
    if ((pathInfo.endsWith(".js") || pathInfo.endsWith(".css"))) {
      return loadScriptFile(servlet, req, pathInfo);
    }
    if (pathInfo.endsWith(".html")) {
      return loadHtmlFile(servlet, req, pathInfo);
    }
    if (pathInfo.endsWith(".json")) {
      return loadJsonFile(servlet, req, pathInfo);
    }
    if (pathInfo.matches("^/dynamic/.*")) {
      return loadDynamicAdapterResource(servlet, req, pathInfo);
    }
    return loadBinaryFile(servlet, req, pathInfo);
  }

  /**
   * This method loads static icon images from {@link IconLocator} (<code>/resource/icons</code> folders of all jars on
   * the classpath).
   */
  protected HttpCacheObject loadIcon(UiServlet servlet, HttpServletRequest req, String pathInfo) {
    final String imageId = pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
    IconSpec iconSpec = IconLocator.instance().getIconSpec(imageId);
    if (iconSpec != null) {
      // cache: use max-age caching for at most 4 hours
      BinaryResource content = new BinaryResource(iconSpec.getName(), detectContentType(servlet, pathInfo), iconSpec.getContent(), System.currentTimeMillis());
      return new HttpCacheObject(pathInfo, true, IHttpCacheControl.MAX_AGE_4_HOURS, content);
    }
    return null;
  }

  /**
   * js, css
   */
  protected HttpCacheObject loadScriptFile(UiServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    ScriptFileBuilder builder = new ScriptFileBuilder(BEANS.get(IWebContentService.class), getSharedScriptProcessor());
    builder.setMinifyEnabled(UiHints.isMinifyHint(req));
    ScriptOutput out = builder.buildScript(pathInfo);
    if (out != null) {
      BinaryResource content = new BinaryResource(out.getPathInfo(), detectContentType(servlet, pathInfo), out.getContent(), out.getLastModified());
      // TODO BSH Check with IMO: Why not pass MAX_AGE_ONE_YEAR instead of -1? Would make logic in DefaultHttpCacheControl.getMaxAgeFor() obsolete
      return new HttpCacheObject(pathInfo, true, -1, content);
    }
    return null;
  }

  /**
   * html
   */
  protected HttpCacheObject loadHtmlFile(UiServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    byte[] bytes = IOUtility.readFromUrl(url);
    bytes = replaceHtmlScriptTags(servlet, req, bytes);
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(servlet, pathInfo), bytes, System.currentTimeMillis());
    // TODO BSH Check with IMO: Why not explicitly pass MAX_AGE_4_HOURS instead of -1? Would make logic in DefaultHttpCacheControl.getMaxAgeFor() obsolete
    return new HttpCacheObject(pathInfo, true, -1, content);
  }

  /**
   * json
   */
  protected HttpCacheObject loadJsonFile(UiServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    // TODO BSH Maybe optimize memory consumption (unnecessary conversion of byte[] to String)
    String json = new String(IOUtility.readFromUrl(url), UTF_8);
    json = JsonUtility.stripCommentsFromJson(json);
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(servlet, pathInfo), json.getBytes(UTF_8), System.currentTimeMillis());
    // TODO BSH Check with IMO: Why not explicitly pass MAX_AGE_4_HOURS instead of -1? Would make logic in DefaultHttpCacheControl.getMaxAgeFor() obsolete
    return new HttpCacheObject(pathInfo, true, -1, content);
  }

  /**
   * This method loads resources that are temporary or dynamically registered on the {@link IUiSession}. This includes
   * adapter/form-fields such as the image field, WordAddIn docx documents, temporary and time-limited landing page
   * files etc.
   * <p>
   * The pathInfo is expected to have the following form: <code>/dynamic/[uiSessionId]/[adapterId]/[filename]</code>
   */
  protected HttpCacheObject loadDynamicAdapterResource(UiServlet servlet, HttpServletRequest req, String pathInfo) {
    Matcher m = PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH.matcher(pathInfo);
    if (!m.matches()) {
      return null;
    }
    String uiSessionId = m.group(1);
    String adapterId = m.group(2);
    String filename = m.group(3);

    HttpSession httpSession = req.getSession();
    IUiSession uiSession = (IUiSession) httpSession.getAttribute(IUiSession.HTTP_SESSION_ATTRIBUTE_PREFIX + uiSessionId);
    if (uiSession == null) {
      return null;
    }
    IJsonAdapter<?> jsonAdapter = uiSession.getJsonAdapter(adapterId);
    if (!(jsonAdapter instanceof IBinaryResourceProvider)) {
      return null;
    }
    IBinaryResourceProvider provider = (IBinaryResourceProvider) jsonAdapter;
    BinaryResource content0 = provider.loadDynamicResource(filename);
    if (content0 == null) {
      return null;
    }
    String contentType = content0.getContentType();
    if (contentType == null) {
      contentType = detectContentType(servlet, pathInfo);
    }
    BinaryResource content = new BinaryResource(pathInfo, contentType, content0.getContent(), content0.getLastModified());

    // TODO BSH Check with IMO: Why not explicitly pass MAX_AGE_4_HOURS instead of -1? Would make logic in DefaultHttpCacheControl.getMaxAgeFor() obsolete
    return new HttpCacheObject(pathInfo, content.getLastModified() > 0, -1, content);
  }

  /**
   * Static binary file png, jpg, woff, pdf, docx
   */
  protected HttpCacheObject loadBinaryFile(UiServlet servlet, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    byte[] bytes = IOUtility.readFromUrl(url);
    URLConnection uc = url.openConnection();
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(servlet, pathInfo), bytes, uc.getLastModified());
    // TODO BSH Check with IMO: Why not explicitly pass MAX_AGE_4_HOURS instead of -1? Would make logic in DefaultHttpCacheControl.getMaxAgeFor() obsolete
    return new HttpCacheObject(pathInfo, true, -1, content);
  }

  protected boolean isDownload(BinaryResource res) {
    return DOWNLOAD_CONTENT_TYPES.contains(res.getContentType());
  }

  protected String detectContentType(UiServlet servlet, String path) {
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
   * Process all js and css script tags that contain the marker text "fingerprint". The marker text is replaced by the
   * effective files {@link HttpCacheObject#getFingerprint()} in hex format
   */
  protected byte[] replaceHtmlScriptTags(UiServlet servlet, HttpServletRequest req, byte[] content) throws IOException, ServletException {
    String oldHtml = new String(content, UTF_8);
    Matcher m = ScriptFileBuilder.SCRIPT_URL_PATTERN.matcher(oldHtml);
    StringBuilder buf = new StringBuilder();
    int lastEnd = 0;
    int replaceCount = 0;
    while (m.find()) {
      buf.append(oldHtml.substring(lastEnd, m.start()));
      if ("fingerprint".equals(m.group(4))) {
        replaceCount++;
        String fingerprint = null;
        if (UiHints.isCacheHint(req)) {
          HttpCacheObject obj = loadScriptFile(servlet, req, m.group());
          if (obj == null) {
            LOG.warn("Failed to locate resource referenced in html file '" + req.getPathInfo() + "': " + m.group());
          }
          fingerprint = (obj != null ? Long.toHexString(obj.getResource().getFingerprint()) : m.group(4));
        }
        buf.append(m.group(1));
        buf.append(m.group(2));
        buf.append("-");
        buf.append(m.group(3));
        if (fingerprint != null) {
          buf.append("-");
          buf.append(fingerprint);
        }
        if (UiHints.isMinifyHint(req)) {
          buf.append(".min");
        }
        buf.append(".");
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
    if (LOG.isTraceEnabled()) {
      LOG.trace("process html script tags:\nINPUT\n" + oldHtml + "\n\nOUTPUT\n" + newHtml);
    }
    return newHtml.getBytes(UTF_8);
  }
}
