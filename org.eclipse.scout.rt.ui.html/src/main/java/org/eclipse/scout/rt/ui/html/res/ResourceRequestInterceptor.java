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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiHints;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.HttpResponseHeaderContributor;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonUtility;
import org.eclipse.scout.rt.ui.html.res.HtmlDocumentParserParameters.IScriptFileLoader;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;
import org.eclipse.scout.rt.ui.html.script.ScriptOutput;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * This interceptor contributes to the {@link UiServlet} as the default GET handler for
 * <p>
 * js, css, html, png, gif, jpg, woff, json
 */
@Order(20)
public class ResourceRequestInterceptor implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ResourceRequestInterceptor.class);

  public static final String INDEX_HTML = "/index.html";
  public static final String MOBILE_INDEX_HTML = "/index-mobile.html";

  private ScriptProcessor m_scriptProcessor;

  @Override
  public boolean interceptGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = resolvePathInfo(req);
    LOG.debug("processing resource request: " + pathInfo);

    // lookup cache or load
    IHttpCacheControl httpCacheControl = BEANS.get(IHttpCacheControl.class);
    HttpCacheObject cacheObj = httpCacheControl.getCacheObject(req, pathInfo);
    if (cacheObj == null) {
      cacheObj = loadResource(servlet.getServletContext(), req, pathInfo);
      // store in cache
      if (cacheObj != null) {
        httpCacheControl.putCacheObject(req, cacheObj);
      }
    }

    // check object existence
    if (cacheObj == null) {
      return false;
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

    // Apply response interceptors
    cacheObj.applyHttpResponseInterceptors(servlet, req, resp);

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
      // Return index-mobile.html, but only if index-mobile.html exists (project may decide to always use index.html)
      URL url = BEANS.get(IWebContentService.class).getWebContentResource(MOBILE_INDEX_HTML);
      if (url != null) {
        return MOBILE_INDEX_HTML;
      }
    }
    return INDEX_HTML;
  }

  /**
   * Loads a resource with an appropriate method, based on the URL
   */
  protected HttpCacheObject loadResource(ServletContext context, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    if (pathInfo.matches("^/icon/.*")) {
      return loadIcon(context, req, pathInfo);
    }
    if (pathInfo.matches("^/dynamic/.*")) {
      return loadDynamicAdapterResource(context, req, pathInfo);
    }
    if ((pathInfo.endsWith(".js") || pathInfo.endsWith(".css"))) {
      return loadScriptFile(context, req, pathInfo);
    }
    if (pathInfo.endsWith(".html")) {
      return loadHtmlFile(context, req, pathInfo);
    }
    if (pathInfo.endsWith(".json")) {
      return loadJsonFile(context, req, pathInfo);
    }
    return loadBinaryFile(context, req, pathInfo);
  }

  /**
   * This method loads static icon images from {@link IconLocator} (<code>/resource/icons</code> folders of all jars on
   * the classpath).
   */
  protected HttpCacheObject loadIcon(ServletContext context, HttpServletRequest req, String pathInfo) {
    final String imageId = pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
    IconSpec iconSpec = IconLocator.instance().getIconSpec(imageId);
    if (iconSpec != null) {
      // cache: use max-age caching for at most 4 hours
      BinaryResource content = new BinaryResource(iconSpec.getName(), detectContentType(context, pathInfo), iconSpec.getContent(), System.currentTimeMillis());
      return new HttpCacheObject(pathInfo, true, IHttpCacheControl.MAX_AGE_4_HOURS, content);
    }
    return null;
  }

  /**
   * js, css
   */
  protected HttpCacheObject loadScriptFile(ServletContext context, HttpServletRequest req, String pathInfo) throws IOException {
    ScriptFileBuilder builder = new ScriptFileBuilder(BEANS.get(IWebContentService.class), getSharedScriptProcessor());
    builder.setMinifyEnabled(UiHints.isMinifyHint(req));
    ScriptOutput out = builder.buildScript(pathInfo);
    if (out != null) {
      BinaryResource content = new BinaryResource(out.getPathInfo(), detectContentType(context, pathInfo), out.getContent(), out.getLastModified());
      return new HttpCacheObject(pathInfo, true, IHttpCacheControl.MAX_AGE_ONE_YEAR, content);
    }
    return null;
  }

  /**
   * html
   */
  protected HttpCacheObject loadHtmlFile(ServletContext context, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    byte[] bytes = IOUtility.readFromUrl(url);
    HtmlDocumentParserParameters params = createHtmlDocumentParserParameters(context, req);
    HtmlDocumentParser parser = new HtmlDocumentParser(params, bytes);
    bytes = parser.parseDocument();
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(context, pathInfo), bytes, System.currentTimeMillis());
    // no cache-control, only E-Tag checks to make sure that a session with timeout is correctly
    // forwarded to the login using a GET request BEFORE the first json POST request
    HttpCacheObject httpCacheObject = new HttpCacheObject(pathInfo, true, -1, content);
    // Suppress automatic "compatibility mode" in IE in intranet zone
    httpCacheObject.addHttpResponseInterceptor(new HttpResponseHeaderContributor("X-UA-Compatible", "IE=edge") {
      private static final long serialVersionUID = 1L;

      @Override
      public void intercept(UiServlet httpServlet, HttpServletRequest httpReq, HttpServletResponse httpResp) {
        BrowserInfo browserInfo = BrowserInfo.createFrom(httpReq);
        if (browserInfo.isMshtml()) {
          // Send headers only for IE
          super.intercept(httpServlet, httpReq, httpResp);
        }
        return;
      }
    });
    return httpCacheObject;
  }

  private HtmlDocumentParserParameters createHtmlDocumentParserParameters(final ServletContext context, final HttpServletRequest req) {
    HtmlDocumentParserParameters params = new HtmlDocumentParserParameters();
    params.setMinify(UiHints.isMinifyHint(req));
    params.setCacheEnabled(UiHints.isCacheHint(req));
    params.setScriptFileLoader(new IScriptFileLoader() {
      @Override
      public HttpCacheObject loadScriptFile(String scriptPath) throws IOException {
        return ResourceRequestInterceptor.this.loadScriptFile(context, req, scriptPath);
      }
    });
    return params;
  }

  /**
   * json
   */
  protected HttpCacheObject loadJsonFile(ServletContext context, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    // TODO BSH Maybe optimize memory consumption (unnecessary conversion of byte[] to String)
    String json = new String(IOUtility.readFromUrl(url), Encoding.UTF_8);
    json = JsonUtility.stripCommentsFromJson(json);
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(context, pathInfo), json.getBytes(Encoding.UTF_8), System.currentTimeMillis());
    return new HttpCacheObject(pathInfo, true, IHttpCacheControl.MAX_AGE_4_HOURS, content);
  }

  /**
   * This method loads resources that are temporary or dynamically registered on the {@link IUiSession}. This includes
   * adapter/form-fields such as the image field, WordAddIn docx documents, temporary and time-limited landing page
   * files etc.
   * <p>
   * The pathInfo is expected to have the following form: <code>/dynamic/[uiSessionId]/[adapterId]/[filename]</code>
   */
  protected HttpCacheObject loadDynamicAdapterResource(ServletContext context, HttpServletRequest req, String pathInfo) {
    Matcher m = BinaryResourceUrlUtility.PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH.matcher(pathInfo);
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
    BinaryResourceHolder binaryResource = provider.provideBinaryResource(filename);
    if (binaryResource == null || binaryResource.get() == null) {
      return null;
    }
    String contentType = binaryResource.get().getContentType();
    if (contentType == null) {
      contentType = detectContentType(context, pathInfo);
    }
    BinaryResource content = new BinaryResource(pathInfo, contentType, binaryResource.get().getContent(), binaryResource.get().getLastModified());
    HttpCacheObject httpCacheObject = new HttpCacheObject(pathInfo, content.getLastModified() > 0, IHttpCacheControl.MAX_AGE_4_HOURS, content);
    if (binaryResource.isDownload()) {
      // Set hint for browser to show the "save as" dialog (no in-line display, not even for known types, e.g. XML)
      httpCacheObject.addHttpResponseInterceptor(new HttpResponseHeaderContributor("Content-Disposition", "attachment"));
    }
    return httpCacheObject;
  }

  /**
   * Static binary file png, jpg, woff, pdf, docx
   */
  protected HttpCacheObject loadBinaryFile(ServletContext context, HttpServletRequest req, String pathInfo) throws ServletException, IOException {
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      //not handled here
      return null;
    }
    byte[] bytes = IOUtility.readFromUrl(url);
    URLConnection uc = url.openConnection();
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(context, pathInfo), bytes, uc.getLastModified());
    return new HttpCacheObject(pathInfo, true, IHttpCacheControl.MAX_AGE_4_HOURS, content);
  }

  protected String detectContentType(ServletContext context, String path) {
    int lastSlash = path.lastIndexOf('/');
    String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    //Prefer mime type mapping from container
    String contentType = context.getMimeType(fileName);
    if (contentType != null) {
      return contentType;
    }
    int lastDot = path.lastIndexOf('.');
    String fileExtension = lastDot >= 0 ? path.substring(lastDot + 1) : path;
    return FileUtility.getContentTypeForExtension(fileExtension);
  }

}
