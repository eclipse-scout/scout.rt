/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Used in {@link HttpCacheControl} in order to decide caching strategy and set caching headers
 */
public class HttpCacheObject implements Serializable {
  private static final long serialVersionUID = 1L;

  private final HttpCacheKey m_cacheKey;
  private final BinaryResource m_resource;
  private final Set<IHttpResponseInterceptor> m_httpResponseInterceptors = new HashSet<>();

  /**
   * @param cacheKey
   *          not null
   * @param resource
   *          not null
   */
  public HttpCacheObject(HttpCacheKey cacheKey, BinaryResource resource) {
    m_cacheKey = Assertions.assertNotNull(cacheKey);
    m_resource = Assertions.assertNotNull(resource);
  }

  public HttpCacheKey getCacheKey() {
    return m_cacheKey;
  }

  public boolean isCachingAllowed() {
    return m_resource.isCachingAllowed();
  }

  public int getCacheMaxAge() {
    return m_resource.getCacheMaxAge();
  }

  public BinaryResource getResource() {
    return m_resource;
  }

  /**
   * @return an ETAG if the resource's {@link BinaryResource#getContentLength()} and
   *         {@link BinaryResource#getFingerprint()} are both not -1
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

  public void applyHttpResponseInterceptors(HttpServletRequest req, HttpServletResponse resp) {
    if (resp != null) {
      for (IHttpResponseInterceptor interceptor : m_httpResponseInterceptors) {
        interceptor.intercept(req, resp);
      }
    }
  }
}
