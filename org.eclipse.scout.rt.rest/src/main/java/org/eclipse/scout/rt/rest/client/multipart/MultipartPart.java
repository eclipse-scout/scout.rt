/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client.multipart;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.util.FileUtility;

/**
 * A single part within a {@link MultipartMessage} to be added via {@link MultipartMessage#addPart(MultipartPart)}.
 * <p>
 * Use one of the following methods to create a part:
 * <ul>
 * <li>{@link #ofFile(String, String, InputStream)}
 * <li>{@link #ofField(String, String)}
 * <li>{@link #of(String, String, String, InputStream)}
 * </ul>
 */
public final class MultipartPart {

  private final String m_partName;
  private final String m_filename;
  private final String m_contentType;
  private final InputStream m_inputStream;
  private final Map<String, Object> m_customHeaders;

  private MultipartPart(String partName, String filename, String contentType, InputStream inputStream, Map<String, Object> customHeaders) {
    m_partName = assertNotNull(partName, "name is required");
    m_filename = filename; // filename is optional (only for file field part, not text field part)
    m_contentType = contentType; // content type is optional
    m_inputStream = assertNotNull(inputStream, "inputStream is required");
    m_customHeaders = customHeaders;
  }

  /**
   * Creates a generic part. For most scenarios either {@link #ofFile(String, String, InputStream)} or
   * {@link #ofField(String, String)} can be used instead.
   *
   * @param partName
   *     Name of part (mandatory)
   * @param filename
   *     Filename (optional)
   * @param contentType
   *     Content type (optional)
   * @param inputStream
   *     Input stream (mandatory)
   */
  public static MultipartPart of(String partName, String filename, String contentType, InputStream inputStream) {
    return of(partName, filename, contentType, inputStream, null);
  }

  /**
   * Creates a generic part. For most scenarios either {@link #ofFile(String, String, InputStream)} or
   * {@link #ofField(String, String)} can be used instead.
   *
   * @param partName
   *     Name of part (mandatory)
   * @param filename
   *     Filename (optional)
   * @param contentType
   *     Content type (optional)
   * @param inputStream
   *     Input stream (mandatory)
   * @param customHeaders
   *     Custom header attributes (optional)
   */
  public static MultipartPart of(String partName, String filename, String contentType, InputStream inputStream, Map<String, Object> customHeaders) {
    return new MultipartPart(partName, filename, contentType, inputStream, customHeaders);
  }

  /**
   * Create a file field part.
   *
   * @param partName
   *     Name of part (mandatory)
   * @param filename
   *     Filename (recommended because content type is deduced from filename extension, otherwise
   *     {@link MediaType#APPLICATION_OCTET_STREAM} is used)
   * @param inputStream
   *     Input stream (mandatory)
   */
  public static MultipartPart ofFile(String partName, String filename, InputStream inputStream) {
    return ofFile(partName, filename, inputStream, null);
  }

  /**
   * Create a file field part.
   *
   * @param partName
   *     Name of part (mandatory)
   * @param filename
   *     Filename (recommended because content type is deduced from filename extension, otherwise
   *     {@link MediaType#APPLICATION_OCTET_STREAM} is used)
   * @param inputStream
   *     Input stream (mandatory)
   * @param customHeaders
   *     Custom header attributes (optional)
   */
  public static MultipartPart ofFile(String partName, String filename, InputStream inputStream, Map<String, Object> customHeaders) {
    String contentType = FileUtility.getContentTypeForExtension(FileUtility.getFileExtension(filename));
    return new MultipartPart(partName, filename, contentType, inputStream, customHeaders);
  }

  /**
   * Creates a text field part, uses {@link StandardCharsets#UTF_8} for value encoding and no content type.
   *
   * @param partName
   *     Name of part (mandatory)
   * @param value
   *     Text value (mandatory)
   */
  public static MultipartPart ofField(String partName, String value) {
    assertNotNull(value, "value is required");
    return new MultipartPart(partName, null, null, new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), null);
  }

  public String getPartName() {
    return m_partName;
  }

  public String getFilename() {
    return m_filename;
  }

  public String getContentType() {
    return m_contentType;
  }

  public InputStream getInputStream() {
    return m_inputStream;
  }

  public Map<String, Object> getCustomHeaders() {
    return m_customHeaders;
  }

  @Override
  public String toString() {
    return MultipartPart.class.getSimpleName() + "[m_partName=" + m_partName + " m_filename=" + m_filename + " m_contentType=" + m_contentType + " m_customHeaders=" + m_customHeaders + "]";
  }
}
