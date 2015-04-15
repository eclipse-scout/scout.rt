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
package org.eclipse.scout.rt.ui.html.cache;

import java.io.Serializable;

import org.eclipse.scout.rt.shared.data.basic.BinaryResource;

/**
 * Used in {@link IHttpCacheControl}
 */
public class HttpCacheObject implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_cacheId;
  private final boolean m_cachingAllowed;
  private final int m_cacheMaxAge;
  private final BinaryResource m_resource;

  public HttpCacheObject(String cacheId, boolean cachingAllowed, int cacheMaxAge, BinaryResource resource) {
    m_cacheId = cacheId;
    m_cachingAllowed = cachingAllowed;
    m_cacheMaxAge = cacheMaxAge;
    m_resource = resource;
  }

  public String getCacheId() {
    return m_cacheId;
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
}
