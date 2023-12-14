/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.server.commons.servlet.cache.GlobalHttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
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
@Order(5900) // should be the last request handler, because it is not restricted to a path prefix (e.g. it consumes all *.html files)
public class ResourceRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceRequestHandler.class);

  public static final String INDEX_HTML = "/index.html";
  public static final String URL_PARAM_CLEAR_CACHE = "clearCache";

  // Remember bean instances to save lookups on each GET request
  private final List<ResourceLoaders> m_resourceLoaders = Collections.unmodifiableList(BEANS.all(ResourceLoaders.class));
  private final HttpCacheControl m_httpCacheControl = BEANS.get(HttpCacheControl.class);

  @Override
  public boolean handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfoEx = resolvePathInfoEx(req);

    IResourceLoader resourceLoader = createLoaderFor(req, pathInfoEx);
    if (resourceLoader == null) {
      return false; // no loader for this resource request
    }

    clearCacheIfNecessary(req);

    HttpCacheObject cachedObject = resolveResourceFromCache(req, pathInfoEx, resourceLoader);
    boolean valid = resourceLoader.validateResource(pathInfoEx, cachedObject);
    if (!valid) {
      return false; // not valid
    }

    // cached in browser? -> returns 304 if the resource has not been modified
    if (m_httpCacheControl.checkAndSetCacheHeaders(req, resp, cachedObject)) {
      return true;
    }

    // set the resp headers only if no 304 (according to spec: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5)
    writeResourceToResponse(req, resp, cachedObject);
    return true;
  }

  protected void writeResourceToResponse(HttpServletRequest req, HttpServletResponse resp, HttpCacheObject cachedObject) throws IOException {
    BinaryResource binaryResource = cachedObject.getResource();
    setHttpResponseHeaders(resp, binaryResource);

    // Apply response interceptors
    cachedObject.applyHttpResponseInterceptors(req, resp);

    if (!"HEAD".equals(req.getMethod())) {
      resp.getOutputStream().write(binaryResource.getContent());
    }
  }

  /**
   * Clear global cache (only allowed in development mode). This allows to work with ?cache=true and clears the cache
   * only when required --> rebuilds script/less files.
   */
  protected void clearCacheIfNecessary(HttpServletRequest req) {
    if (Platform.get().inDevelopmentMode() && req.getParameter(URL_PARAM_CLEAR_CACHE) != null) {
      BEANS.get(GlobalHttpResourceCache.class).clear();
      LOG.info("Resource cache has been cleared, requested by URL parameter {}", URL_PARAM_CLEAR_CACHE);
    }
  }

  protected IResourceLoader createLoaderFor(HttpServletRequest req, String requestedExternalPath) {
    for (ResourceLoaders loaderFactory : m_resourceLoaders) {
      IResourceLoader loader = loaderFactory.create(req, requestedExternalPath);
      if (loader != null) {
        return loader;
      }
    }
    return null;
  }

  protected HttpCacheObject resolveResourceFromCache(HttpServletRequest req, String pathInfoEx, IResourceLoader resourceLoader) throws IOException {
    // Create cache key for resource and check if resource exists in cache
    HttpCacheKey cacheKey = resourceLoader.createCacheKey(pathInfoEx);

    HttpCacheObject resource = null;

    // When caching is disabled, always load resource
    if (!UrlHints.isCacheHint(req)) {
      LOG.debug("Requested resource with cacheKey={}. Caching is disabled by URL hint", cacheKey);
      return resourceLoader.loadResource(cacheKey);
    }

    IHttpResourceCache resourceCache = resourceLoader.getCache(cacheKey);
    if (resourceCache == null) {
      LOG.debug("Loader for resource with cacheKey={} does not support caching.", cacheKey);
      return resourceLoader.loadResource(cacheKey);
    }

    String cacheResultMsg;
    resource = resourceCache.get(cacheKey);
    if (resource == null) {
      // Cache miss: resource not found in cache --> load it
      resource = resourceLoader.loadResource(cacheKey);
      if (resource == null) {
        cacheResultMsg = "Resource is not cached (cache miss), could not load resource (not added to the cache)";
      }
      else {
        resourceCache.put(resource);
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
