/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.net.URI;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryRefs;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.collection.TTLCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loads binaryRef resources.
 * <p>
 * The pathInfo is expected to have the following form: <code>/binref/[path]/[id]</code>
 */
public class BinaryRefResourceLoader extends AbstractResourceLoader {

  private static final Logger LOG = LoggerFactory.getLogger(BinaryRefResourceLoader.class);

  private static final String BINARY_REF_CACHE = "binaryRef.cache";

  private final HttpServletRequest m_req;

  public BinaryRefResourceLoader(HttpServletRequest req) {
    super(null /* no instance required as getCache() is overridden */);
    m_req = req;
  }

  public HttpServletRequest getRequest() {
    return m_req;
  }

  @Override
  public IHttpResourceCache getCache(HttpCacheKey cacheKey) {
    HttpSession httpSession = getRequest().getSession();
    if (httpSession == null) {
      return null;
    }

    Object o = httpSession.getAttribute(BINARY_REF_CACHE);
    if (o instanceof IHttpResourceCache) {
      return (IHttpResourceCache) o;
    }

    IHttpResourceCache cache = createCache();
    httpSession.setAttribute(BINARY_REF_CACHE, cache);
    return cache;
  }

  protected IHttpResourceCache createCache() {
    return new IHttpResourceCache() {
      private final TTLCache<HttpCacheKey, HttpCacheObject> m_cache = new TTLCache<>(300000); // 5 minutes (5 * 60 * 1000)

      @Override
      public boolean put(HttpCacheObject obj) {
        if (obj == null) {
          return false;
        }
        if (obj.getResource() != null && obj.getResource().isCachingAllowed()) {
          // this will be cached by the browser, so there is no need to cache it here
          return false;
        }
        m_cache.put(obj.getCacheKey(), obj);
        return true;
      }

      @Override
      public HttpCacheObject get(HttpCacheKey cacheKey) {
        return m_cache.get(cacheKey);
      }

      @Override
      public HttpCacheObject remove(HttpCacheKey cacheKey) {
        return m_cache.remove(cacheKey);
      }

      @Override
      public void clear() {
        m_cache.clear();
      }
    };
  }

  @Override
  public BinaryResource loadResource(String pathInfo) {
    HttpSession httpSession = getRequest().getSession();
    if (httpSession == null) {
      return null;
    }

    BinaryRefResourceInfo info = createBinaryRefResourceInfo(pathInfo);
    if (info == null) {
      return null;
    }

    URI uri = info.toBinaryRefUri();

    // binaryRefs must be independent of the IClientSession, therefore take any
    IClientSession clientSession = BEANS.get(HttpSessionHelper.class)
        .getSessionStore(httpSession)
        .getClientSessionMap().entrySet().stream()
        .findAny()
        .map(Entry::getValue)
        .orElse(null);

    if (clientSession == null) {
      return null;
    }

    try {
      return ClientRunContexts.copyCurrent()
          .withSession(clientSession, true)
          .call(() -> BinaryRefs.loadBinaryResource(uri));
    }
    catch (Exception e) {
      LOG.warn("Unable to load binary resource for URI {}", uri, e);
    }
    return null;
  }

  protected BinaryRefResourceInfo createBinaryRefResourceInfo(HttpCacheKey cacheKey) {
    return createBinaryRefResourceInfo(cacheKey.getResourcePath());
  }

  protected BinaryRefResourceInfo createBinaryRefResourceInfo(String pathInfo) {
    return BinaryRefResourceInfo.fromPath(pathInfo);
  }
}
