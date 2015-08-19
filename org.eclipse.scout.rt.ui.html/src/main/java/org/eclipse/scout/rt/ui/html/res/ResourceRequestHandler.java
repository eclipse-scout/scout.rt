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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.res.loader.IResourceLoader;
import org.eclipse.scout.rt.ui.html.res.loader.ResourceLoaderFactory;

/**
 * This handler contributes to the {@link UiServlet} as the default GET handler for
 * <p>
 * /dynamic/*, /icon/*, *.js, *.css, *.html, *.png, *.gif, *.jpg, *.woff, *.json
 */
@Order(20)
public class ResourceRequestHandler extends AbstractUiServletRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ResourceRequestHandler.class);

  public static final String INDEX_HTML = "/index.html";
  public static final String MOBILE_INDEX_HTML = "/index-mobile.html";

  // Remember bean instances to save lookups on each GET request
  private ResourceLoaderFactory m_resourceLoaderFactory = BEANS.get(ResourceLoaderFactory.class);
  private IHttpCacheControl m_httpCacheControl = BEANS.get(IHttpCacheControl.class);

  @Override
  public boolean handleGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String resourcePath = resolveResourcePath(req);

    // Create loader for the requested resource type
    IResourceLoader resourceLoader = m_resourceLoaderFactory.createResourceLoader(req, resourcePath);

    // Create cache key for resource and check if resource exists in cache
    HttpCacheKey cacheKey = resourceLoader.createCacheKey(resourcePath, NlsLocale.get(false));
    HttpCacheObject resource = m_httpCacheControl.getCacheObject(req, cacheKey);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Lookup resource in cache: " + cacheKey + " (found=" + (resource != null) + ")");
    }
    if (resource == null) {
      // Resource not found in cache --> load it
      resource = resourceLoader.loadResource(cacheKey);
      if (resource != null) {
        m_httpCacheControl.putCacheObject(req, resource);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Stored resource in cache: " + cacheKey);
        }
      }
    }

    // check resource existence
    if (resource == null) {
      return false;
    }

    String contentType = resource.getResource().getContentType();
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    else {
      LOG.warn("Could not determine content type of resource: " + resourcePath);
    }
    resp.setContentLength(resource.getResource().getContentLength());

    // cached in browser? -> returns 304 if the resource has not been modified
    // Important: Check is only done if the request still processes the requested resource
    // and hasn't been forwarded to another one (using req.getRequestDispatcher().forward)
    String originalPathInfo = (String) req.getAttribute("javax.servlet.forward.path_info");
    if (originalPathInfo == null || resourcePath.equals(originalPathInfo)) {
      if (m_httpCacheControl.checkAndUpdateCacheHeaders(req, resp, resource)) {
        return true;
      }
    }

    // Apply response interceptors
    resource.applyHttpResponseInterceptors(servlet, req, resp);

    if (!"HEAD".equals(req.getMethod())) {
      resp.getOutputStream().write(resource.getResource().getContent());
    }
    return true;
  }

  protected ResourceLoaderFactory resourceLoaderFactory() {
    return m_resourceLoaderFactory;
  }

  protected IHttpCacheControl httpCacheControl() {
    return m_httpCacheControl;
  }

  protected String resolveResourcePath(HttpServletRequest req) {
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
}
