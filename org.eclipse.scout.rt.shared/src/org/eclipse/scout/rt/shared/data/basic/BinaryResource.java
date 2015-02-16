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
package org.eclipse.scout.rt.shared.data.basic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.zip.Adler32;

import org.eclipse.scout.commons.CompareUtility;

/**
 * @since 5.0
 */
public final class BinaryResource implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_filename;
  private final String m_contentType;
  private final byte[] m_content;
  private final long m_fingerprint;
  private final long m_lastModified;

  /**
   * @param filename
   * @param content
   * @param lastModified
   *          [milliseconds]
   */
  public BinaryResource(String filename, String contentType, byte[] content, long lastModified) {
    m_filename = filename;
    m_contentType = contentType;
    m_content = content;
    m_lastModified = lastModified;
    if (content != null) {
      Adler32 a = new Adler32();
      a.update(content);
      m_fingerprint = a.getValue();
    }
    else {
      m_fingerprint = -1;
    }
  }

  public String getFilename() {
    return m_filename;
  }

  public String getContentType() {
    return m_contentType;
  }

  public byte[] getContent() {
    return m_content;
  }

  public int getContentLength() {
    return m_content != null ? m_content.length : -1;
  }

  public long getFingerprint() {
    return m_fingerprint;
  }

  public long getLastModified() {
    return m_lastModified;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) m_lastModified;
    result = prime * result + ((m_content == null) ? 0 : m_content.length);
    result = prime * result + ((m_filename == null) ? 0 : m_filename.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BinaryResource other = (BinaryResource) obj;
    return this.m_lastModified == other.m_lastModified &&
        CompareUtility.equals(this.m_filename, other.m_filename) &&
        CompareUtility.equals(this.m_contentType, other.m_contentType) &&
        Arrays.equals(m_content, other.m_content);
  }
}
