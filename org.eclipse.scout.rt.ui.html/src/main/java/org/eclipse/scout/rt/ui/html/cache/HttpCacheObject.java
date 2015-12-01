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
package org.eclipse.scout.rt.ui.html.cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.UiServlet;

/**
 * Used in {@link IHttpCacheControl}
 */
public class HttpCacheObject implements Serializable {
  private static final long serialVersionUID = 1L;

  private final HttpCacheKey m_cacheKey;
  private final boolean m_cachingAllowed;
  private final int m_cacheMaxAge;
  private final BinaryResource m_resource;
  private final Set<IHttpResponseInterceptor> m_httpResponseInterceptors = new HashSet<>();

  public HttpCacheObject(HttpCacheKey cacheKey, boolean cachingAllowed, int cacheMaxAge, BinaryResource resource) {
    m_cacheKey = cacheKey;
    m_cachingAllowed = cachingAllowed;
    m_cacheMaxAge = cacheMaxAge;
    m_resource = resource;
  }

  public HttpCacheKey getCacheKey() {
    return m_cacheKey;
  }

  public boolean isCachingAllowed() {
    return m_cachingAllowed;
  }

  public int getCacheMaxAge() {
    return m_cacheMaxAge;
  }

  public BinaryResource getResource() {
    return m_resource;
  }

  /**
   * @return an ETAG if the resource's {@link #getContentLength()} and {@link #getFingerprint()} are both not -1
   */
  public String createETag() {
    if (m_resource.getFingerprint() != -1L && m_resource.getContentLength() != -1L) {
      return "W/\"" + m_resource.getContentLength() + "-" + m_resource.getFingerprint() + "\"";
    }
    return null;
  }

  public void addHttpResponseInterceptor(IHttpResponseInterceptor interceptor) {
    m_httpResponseInterceptors.add(interceptor);
  }

  public void removeHttpResponseInterceptor(IHttpResponseInterceptor interceptor) {
    m_httpResponseInterceptors.remove(interceptor);
  }

  public void applyHttpResponseInterceptors(UiServlet servlet, HttpServletRequest httpReq, HttpServletResponse httpResp) {
    if (httpResp != null) {
      for (IHttpResponseInterceptor interceptor : m_httpResponseInterceptors) {
        interceptor.intercept(servlet, httpReq, httpResp);
      }
    }
  }
}
