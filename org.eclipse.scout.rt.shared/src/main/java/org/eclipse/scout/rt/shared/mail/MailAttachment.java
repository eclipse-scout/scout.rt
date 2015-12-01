/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.mail;

import java.io.IOException;
import java.util.Arrays;

import javax.activation.DataSource;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.IOUtility;

/**
 * Class representing a mail attachment.
 */
public class MailAttachment {
  private final DataSource m_dataSource;
  private final String m_contentType;
  private final String m_name;
  private final String m_contentId;
  private byte[] m_content;

  public MailAttachment(DataSource dataSource) {
    this(dataSource, null, null, null);
  }

  public MailAttachment(DataSource dataSource, String contentType, String name, String contentId) {
    super();
    if (dataSource == null) {
      throw new IllegalArgumentException("DataSource is missing");
    }
    m_dataSource = dataSource;
    m_contentType = contentType;
    m_name = name;
    m_contentId = contentId;
  }

  public MailAttachment(BinaryResource binaryResource) {
    this(binaryResource, null);
  }

  public MailAttachment(BinaryResource binaryResource, String contentId) {
    this(new BinaryResourceDataSource(binaryResource), binaryResource.getContentType(), binaryResource.getFilename(), contentId);
    m_content = binaryResource.getContent();
  }

  public DataSource getDataSource() {
    return m_dataSource;
  }

  public byte[] getContent() {
    if (m_content == null) {
      try {
        m_content = IOUtility.getContent(m_dataSource.getInputStream());
      }
      catch (IOException e) {
        throw new ProcessingException("Failed to get content", e);
      }
    }
    return Arrays.copyOf(m_content, m_content.length);
  }

  /**
   * @return the content type (MIME type), as passed to the constructor. May be <code>null</code>. In this case, the
   *         name extension might give a hint to determine the content type.
   */
  public String getContentType() {
    return m_contentType;
  }

  /**
   * @return the name, as passed to the constructor. Should not be <code>null</code> (but could).
   */
  public String getName() {
    return m_name;
  }

  public String getContentId() {
    return m_contentId;
  }
}
