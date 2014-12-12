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
  private long m_contentLength = -1;
  private long m_fingerprint = -1;
  private long m_lastModified = -1;
  private int m_preferredCacheMaxAge = -1;

  public HttpCacheInfo() {
  }

  public long getContentLength() {
    return m_contentLength;
  }

  public void setContentLength(long contentLength) {
    m_contentLength = contentLength;
  }

  public long getFingerprint() {
    return m_fingerprint;
  }

  public void setFingerprint(long fingerprint) {
    m_fingerprint = fingerprint;
  }

  public long getLastModified() {
    return m_lastModified;
  }

  public void setLastModified(long lastModified) {
    m_lastModified = lastModified;
  }

  /**
   * @return the preferred max age or 0/negative in oder to omit a max-age header
   */
  public int getPreferredCacheMaxAge() {
    return m_preferredCacheMaxAge;
  }

  public void setPreferredCacheMaxAge(int preferredCacheMaxAge) {
    m_preferredCacheMaxAge = preferredCacheMaxAge;
  }

  /**
   * @return an ETAG if {@link #getContentLength()} and {@link #getFingerprint()} are both not -1
   */
  public String createETag() {
    if (m_fingerprint != -1L && m_contentLength != -1L) {
      return "W/\"" + m_contentLength + "-" + m_fingerprint + "\"";
    }
    return null;
  }
}
