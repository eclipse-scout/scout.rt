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

/**
 * Used in {@link IHttpCacheControl}
 */
public class HttpCacheInfo {
  private final String m_requestPath;
  private final long m_lastModified;
  private final int m_contentLength;

  public HttpCacheInfo(String requestPath, long lastModified, int contentLength) {
    m_requestPath = requestPath;
    m_lastModified = lastModified;
    m_contentLength = contentLength;
  }

  public String getRequestPath() {
    return m_requestPath;
  }

  public long getLastModified() {
    return m_lastModified;
  }

  public int getContentLength() {
    return m_contentLength;
  }

  /**
   * @return an ETAG if {@link #getLastModified()} and {@link #getContentLength()} are both not -1
   */
  public String createETag() {
    if (m_lastModified != -1L && m_contentLength != -1L) {
      return "W/\"" + m_contentLength + "-" + m_lastModified + "\"";
    }
    return null;
  }
}
