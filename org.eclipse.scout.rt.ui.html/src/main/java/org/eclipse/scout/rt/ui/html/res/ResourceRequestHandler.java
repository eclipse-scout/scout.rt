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
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResourceCache;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.res.loader.IResourceLoader;
import org.eclipse.scout.rt.ui.html.res.loader.ResourceLoaders;
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

  // Remember bean instances to save lookups on each GET request
  private final List<ResourceLoaders> m_resourceLoaders = Collections.unmodifiableList(BEANS.all(ResourceLoaders.class));
  private final HttpResourceCache m_httpResourceCache = BEANS.get(HttpResourceCache.class);
  private final HttpCacheControl m_httpCacheControl = BEANS.get(HttpCacheControl.class);

  @Override
  public boolean handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfoEx = resolvePathInfoEx(req);

    // Create loader for the requested resource type
    IResourceLoader resourceLoader = null;
    for (ResourceLoaders f : m_resourceLoaders) {
      resourceLoader = f.create(req, pathInfoEx);
      if (resourceLoader != null) {
        break;
      }
    }

    if (resourceLoader == null) {
      return false;
    }

    HttpCacheObject resource = resolveResourceFromCache(req, pathInfoEx, resourceLoader);

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

  protected HttpCacheObject resolveResourceFromCache(HttpServletRequest req, String pathInfoEx, IResourceLoader resourceLoader) throws IOException {
    // Create cache key for resource and check if resource exists in cache
    HttpCacheKey cacheKey = resourceLoader.createCacheKey(pathInfoEx);

    // When caching is disabled, always load resource
    if (!UrlHints.isCacheHint(req)) {
      LOG.debug("Requested resource with cacheKey={}. Caching is disabled by URL hint");
      return resourceLoader.loadResource(cacheKey);
    }

    String cacheResultMsg;
    HttpCacheObject resource = null;
    resource = m_httpResourceCache.get(cacheKey);
    if (resource == null) {
      // Cache miss: resource not found in cache --> load it
      resource = resourceLoader.loadResource(cacheKey);
      if (resource == null) {
        cacheResultMsg = "Resource is not cached (cache miss), could not load resource (not added to the cache)";
      }
      else {
        m_httpResourceCache.put(resource);
        cacheResultMsg = "Resource is not cached (cache miss), resource loaded and added to the cache";
      }
    }
    else {
      // Cache hit
      cacheResultMsg = "Resource found in cache (cache hit), using cached resource";
    }

    LOG.debug("Requested resource with cacheKey={}. {}", cacheKey, cacheResultMsg);
    return resource;
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

  protected List<ResourceLoaders> resourceLoaders() {
    return m_resourceLoaders;
  }

  protected HttpResourceCache httpResourceCache() {
    return m_httpResourceCache;
  }

  /**
   * @return index.html for requests on root (empty or /) and also for deep-link requests, for all other requests the
   *         pathInfo from the given request
   */
  protected String resolvePathInfoEx(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return null;
    }
    if ("/".equals(pathInfo)) {
      pathInfo = INDEX_HTML;
    }
    return pathInfo;
  }

}
