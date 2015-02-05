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

import java.util.zip.Adler32;

/**
 * Used in {@link IHttpCacheControl}
 */
public class HttpCacheObject {
  private final String m_pathInfo;
  private final byte[] m_content;
  private final long m_lastModified;
  private final long m_fingerprint;

  // FIXME AWE: add m_fileExtension

  public HttpCacheObject(String pathInfo, byte[] content, long lastModified) {
    m_pathInfo = pathInfo;
    m_content = content;
    m_lastModified = lastModified;
    Adler32 a = new Adler32();
    a.update(content);
    m_fingerprint = a.getValue();
  }

  public String getPathInfo() {
    return m_pathInfo;
  }

  public byte[] getContent() {
    return m_content;
  }

  public long getLastModified() {
    return m_lastModified;
  }

  public long getFingerprint() {
    return m_fingerprint;
  }

  /**
   * Convenience for creating a {@link HttpCacheInfo} out of this object
   */
  public HttpCacheInfo toCacheInfo() {
    HttpCacheInfo info = new HttpCacheInfo();
    info.setContentLength(m_content.length);
    info.setFingerprint(m_fingerprint);
    info.setLastModified(m_lastModified);
    return info;
  }
}
