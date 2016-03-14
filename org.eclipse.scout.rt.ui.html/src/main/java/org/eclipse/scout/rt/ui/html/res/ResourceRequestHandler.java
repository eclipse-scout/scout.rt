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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.res.loader.IResourceLoader;
import org.eclipse.scout.rt.ui.html.res.loader.IResourceLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler contributes to the {@link UiServlet} as the default GET handler for
 * <p>
 * /dynamic/*, /icon/*, *.js, *.css, *.html, *.png, *.gif, *.jpg, *.woff, *.json
 */
@Order(5020)
public class ResourceRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceRequestHandler.class);

  public static final String INDEX_HTML = "/index.html";
  public static final String MOBILE_INDEX_HTML = "/index-mobile.html";

  // Remember bean instances to save lookups on each GET request
  private final List<IResourceLoaderFactory> m_resourceLoaderFactoryList = Collections.unmodifiableList(BEANS.all(IResourceLoaderFactory.class));
  private final IHttpCacheControl m_httpCacheControl = BEANS.get(IHttpCacheControl.class);

  @Override
  public boolean handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfoEx = resolvePathInfoEx(req);

    // Create loader for the requested resource type
    IResourceLoader resourceLoader = null;
    for (IResourceLoaderFactory f : m_resourceLoaderFactoryList) {
      resourceLoader = f.createResourceLoader(req, pathInfoEx);
      if (resourceLoader != null) {
        break;
      }
    }

    if (resourceLoader == null) {
      return false;
    }

    // Create cache key for resource and check if resource exists in cache
    HttpCacheKey cacheKey = resourceLoader.createCacheKey(pathInfoEx);
    HttpCacheObject resource = m_httpCacheControl.getCacheObject(req, cacheKey);
    if (resource == null) {
      // Resource not found in cache --> load it
      resource = resourceLoader.loadResource(cacheKey);
      if (resource != null) {
        m_httpCacheControl.putCacheObject(req, resource);
      }
    }

    // check resource existence (also ignore resources without content, to prevent invalid "content-length" header and NPE in write() method)
    if (resource == null || resource.getResource() == null || resource.getResource().getContent() == null) {
      return false;
    }

    // cached in browser? -> returns 304 if the resource has not been modified
    if (m_httpCacheControl.checkAndSetCacheHeaders(req, resp, pathInfoEx, resource)) {
      return true;
    }

    BinaryResource binaryResource = resource.getResource();

    // set the resp headers only if no 304 (according to spec: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5)
    setHttpResponseHeaders(resp, binaryResource);

    // Apply response interceptors
    resource.applyHttpResponseInterceptors(req, resp);

    if (!"HEAD".equals(req.getMethod())) {
      resp.getOutputStream().write(binaryResource.getContent());
    }
    return true;
  }

  /**
   * Sets HTTP response header fields: content-length, content-type (incl. optional charset).
   */
  protected void setHttpResponseHeaders(HttpServletResponse resp, BinaryResource resource) {
    // content-length
    resp.setContentLength(resource.getContentLength());

    // charset
    String charset = resource.getCharset();
    if (charset != null) {
      resp.setCharacterEncoding(charset);
    }

    // content-type
    String contentType = resource.getContentType();
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    else {
      LOG.warn("Could not determine content-type of resource: {}", resource);
    }
  }

  protected List<IResourceLoaderFactory> resourceLoaderFactoryList() {
    return m_resourceLoaderFactoryList;
  }

  protected IHttpCacheControl httpCacheControl() {
    return m_httpCacheControl;
  }

  protected String resolvePathInfoEx(HttpServletRequest req) {
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
    LOG.info("Resolve index html. Browser info: {}", browserInfo);
    if (browserInfo.isMobile()) {
      // Return index-mobile.html, but only if index-mobile.html exists (project may decide to always use index.html)
      URL url = BEANS.get(IWebContentService.class).getWebContentResource(MOBILE_INDEX_HTML);
      if (url != null) {
        LOG.info("Return " + MOBILE_INDEX_HTML);
        return MOBILE_INDEX_HTML;
      }
    }
    return INDEX_HTML;
  }
}
