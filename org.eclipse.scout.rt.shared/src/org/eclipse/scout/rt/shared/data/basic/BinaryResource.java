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
import org.eclipse.scout.commons.FileUtility;

/**
 * Wrapper for binary content (<code>byte[]</code>) with some meta data.
 *
 * @since 5.0
 */
public final class BinaryResource implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_filename;
  private final String m_contentType;
  private final byte[] m_content;
  private final long m_lastModified;

  private final long m_fingerprint;

  /**
   * @param filename
   *          A valid file name (with or without path information), preferably with file extension to allow automatic
   *          detection of MIME type. Examples: <code>"image.jpg"</code>, <code>"icons/eye.png"</code>
   * @param contentType
   *          MIME type of the resource. Example: <code>"image/jpeg"</code>. If this value is omitted, it is recommended
   *          to ensure that the argument <i>filename</i> has a valid file extension, which can then be used to
   *          determine the MIME type.
   *          <p>
   *          null contentType is replaced by {@link FileUtility#getContentTypeForExtension(String)}
   * @param content
   *          The resource's content as byte array. The fingerprint for the given content is calculated automatically.
   * @param lastModified
   *          "Last modified" timestamp of the resource (in milliseconds a.k.a. UNIX time). <code>-1</code> if unknown.
   */
  public BinaryResource(String filename, String contentType, byte[] content, long lastModified) {
    m_filename = filename;
    if (contentType != null) {
      m_contentType = contentType;
    }
    else {
      int i = filename.lastIndexOf('.');
      if (i >= 0) {
        m_contentType = FileUtility.getContentTypeForExtension(filename.substring(i + 1));
      }
      else {
        m_contentType = null;
      }
    }
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

  /**
   * Convenience constructor which assumes <code>contentType = null</code> and <code>lastModified = -1</code>
   * <p>
   * null contentType is replaced by {@link FileUtility#getContentTypeForExtension(String)}
   *
   * @see #BinaryResource(String, String, byte[], long)
   */
  public BinaryResource(String filename, byte[] content) {
    this(filename, null, content, -1);
  }

  /**
   * @return the filename, as passed to the constructor. Should not be <code>null</code> (but could).
   */
  public String getFilename() {
    return m_filename;
  }

  /**
   * @return the content type (MIME type), as passed to the constructor. May be <code>null</code>. In this case,
   *         the filename extension might give a hint to determine the content type.
   */
  public String getContentType() {
    return m_contentType;
  }

  /**
   * @return the raw binary content, as passed to the constructor
   */
  public byte[] getContent() {
    return m_content;
  }

  /**
   * Convenience method to get the length of the byte array returend by {@link #getContent()}. If the content
   * is <code>null</code>, this method returns <code>-1</code>.
   */
  public int getContentLength() {
    return m_content != null ? m_content.length : -1;
  }

  /**
   * @return the "last modified" timestamp in milliseconds (or <code>-1</code> if last modified time is unknown).
   */
  public long getLastModified() {
    return m_lastModified;
  }

  /**
   * @return a checksum-style fingerprint of the binary content. This fingerprint was calculated during the
   *         constructor by applying the Adler32 algorithm to the content. If the content is <code>null</code>, this
   *         method returns <code>-1</code>.
   */
  public long getFingerprint() {
    return m_fingerprint;
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
