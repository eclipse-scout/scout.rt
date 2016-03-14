/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.resource;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.Adler32;

import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Wrapper for binary content (<code>byte[]</code>) with some meta data.
 * <p>
 * All properties are final, thus the binary resource is somehow immutable with the exception of the content where the
 * array could be directly manipulated. Due to performance considerations the content is not duplicated when retrieved.
 *
 * @since 5.0
 */
public final class BinaryResource implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_filename;
  private final String m_contentType;

  /**
   * String instead of {@link java.nio.charset.Charset} because otherwise not serializable.
   */
  private final String m_charset;
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
   *          null contentType is replaced by {@link FileUtility#getMimeType(java.nio.file.Path)}
   * @param content
   *          The resource's content as byte array. The fingerprint for the given content is calculated automatically.
   * @param lastModified
   *          "Last modified" timestamp of the resource (in milliseconds a.k.a. UNIX time). <code>-1</code> if unknown.
   * @deprecated Use {@link BinaryResources} instead. Constructor will be package private in 6.0.
   */
  @Deprecated
  public BinaryResource(String filename, String contentType, String charset, byte[] content, long lastModified) {
    m_filename = filename;
    if (contentType == null) {
      if (filename != null) {
        contentType = FileUtility.getMimeType(Paths.get(filename));
      }
      else if (content != null && content.length > 0) {
        File f = IOUtility.createTempFile("file", content);
        contentType = FileUtility.getMimeType(f.toPath());
        f.delete();
      }
      else {
        contentType = MimeType.APPLICATION_OCTET_STREAM.getType();
      }
    }
    m_contentType = contentType;
    m_charset = charset;
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
   * Convenience constructor which assumes <code>filename = empty, lastModified = -1</code>.
   *
   * @see #BinaryResource(String, String, String, byte[], long)
   * @deprecated Use {@link BinaryResources} instead (use contentType.getType() for string content type).
   */
  @Deprecated
  public BinaryResource(MimeType contentType, byte[] content) {
    this(null, contentType != null ? contentType.getType() : null, null, content, -1);
  }

  /**
   * Convenience constructor which assumes <code>charset = null</code>.
   *
   * @see #BinaryResource(String, String, String, byte[], long)
   * @deprecated Use {@link BinaryResources} instead.
   */
  @Deprecated
  public BinaryResource(String filename, String contentType, byte[] content, long lastModified) {
    this(filename, contentType, null, content, lastModified);
  }

  /**
   * Convenience constructor which assumes <code>lastModified = -1</code>.
   *
   * @see #BinaryResource(String, String, String, byte[], long)
   * @deprecated Use {@link BinaryResources} instead.
   */
  @Deprecated
  public BinaryResource(String filename, String contentType, byte[] content) {
    this(filename, contentType, null, content, -1);
  }

  /**
   * Convenience constructor which assumes <code>contentType = null</code> and <code>lastModified = -1</code>
   * <p>
   * null contentType is replaced by {@link FileUtility#getContentTypeForExtension(String)}
   *
   * @see BinaryResources
   */
  public BinaryResource(String filename, byte[] content) {
    this(filename, null, null, content, -1);
  }

  /**
   * Reads the contents of the given file into a new {@link BinaryResource}. This constructor is intended to read from
   * temporary files only. If the file is not accessible, the constructor will fail.
   *
   * @deprecated Use {@link BinaryResources} to build resource instead and load properties manually (filename:
   *             file.getName(), content type: FileUtility.getContentType(file), content: IOUtility.getContent(file),
   *             last modified: file.lastModified()).
   */
  @Deprecated
  public BinaryResource(File file) {
    this(file.getName(), FileUtility.getContentType(file), null, IOUtility.getContent(file), file.lastModified());
  }

  /**
   * Checks if the filename is not empty and has text.
   */
  public boolean hasFilename() {
    return StringUtility.hasText(getFilename());
  }

  /**
   * @return the filename, as passed to the constructor. Should not be <code>null</code> (but could).
   */
  public String getFilename() {
    return m_filename;
  }

  /**
   * @return the content type (MIME type), as passed to the constructor. Is never null.
   */
  public String getContentType() {
    return m_contentType;
  }

  /**
   * @return the charset (character encoding), as passed to the constructor. May be <code>null</code> for non-text
   *         resources.
   */
  public String getCharset() {
    return m_charset;
  }

  /**
   * Do not modify the returned array, the binary resource is assumed to be immutable (returned content is not cloned
   * due to performance considerations).
   *
   * @return the raw binary content, as passed to the constructor
   */
  public byte[] getContent() {
    return m_content;
  }

  /**
   * @return the {@link String} content (using defined {@link Charset} or UTF-8 as default) for this resource
   */
  public String getContentAsString() {
    Charset charset = StandardCharsets.UTF_8;
    if (getCharset() != null) {
      charset = Charset.forName(getCharset());
    }
    return new String(m_content, charset);
  }

  /**
   * Convenience method to get the length of the byte array returend by {@link #getContent()}. If the content is
   * <code>null</code>, this method returns <code>-1</code>.
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
   * @return a checksum-style fingerprint of the binary content. This fingerprint was calculated during the constructor
   *         by applying the Adler32 algorithm to the content. If the content is <code>null</code>, this method returns
   *         <code>-1</code>.
   */
  public long getFingerprint() {
    return m_fingerprint;
  }

  /**
   * @return a new {@link BinaryResource} that represents the same content, but has another name
   */
  public BinaryResource createAlias(String newName) {
    return BinaryResources.create(this).withFilename(newName).build();
  }

  /**
   * @return a new {@link BinaryResource} that represents the same content, but has another name with the same file
   *         extension
   */
  public BinaryResource createAliasWithSameExtension(String newNameWithoutExtension) {
    String fileExtension = FileUtility.getFileExtension(m_filename);
    String newName = newNameWithoutExtension + (fileExtension == null ? "" : "." + fileExtension);
    return createAlias(newName);
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
    return this.m_lastModified == other.m_lastModified
        && CompareUtility.equals(this.m_filename, other.m_filename)
        && CompareUtility.equals(this.m_contentType, other.m_contentType)
        && Arrays.equals(m_content, other.m_content);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append("content: ");
    if (m_content == null) {
      sb.append("null");
    }
    else {
      sb.append(m_content.length).append(" bytes");
    }
    if (m_filename != null) {
      sb.append(", filename: ").append(m_filename);
    }
    if (m_contentType != null) {
      sb.append(", contentType: ").append(m_contentType);
    }
    if (m_charset != null) {
      sb.append(", charset: ").append(m_charset);
    }
    if (m_lastModified != -1) {
      sb.append(", lastModified: ").append(m_lastModified);
    }
    if (m_fingerprint != -1) {
      sb.append(", fingerprint: ").append(m_fingerprint);
    }
    sb.append("]");
    return sb.toString();
  }
}
