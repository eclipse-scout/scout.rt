/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.DataSource;
import jakarta.mail.util.SharedByteArrayInputStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * Data source for binary resource.
 *
 * @since 6.0
 */
public class BinaryResourceDataSource implements DataSource {
  private final BinaryResource m_binaryResource;
  private final String m_contentType;

  public BinaryResourceDataSource(BinaryResource binaryResource) {
    if (binaryResource == null) {
      throw new IllegalArgumentException("Binary resource is missing");
    }
    m_binaryResource = binaryResource;

    // use content type of binary resource if provided
    // otherwise try to determine content type by file extension
    // otherwise use application/octet-stream
    String contentType = binaryResource.getContentType();
    if (contentType == null) {
      String filename = binaryResource.getFilename();
      if (filename != null) {
        int indexDot = filename.lastIndexOf('.');
        if (indexDot > 0) {
          String fileExtension = null;
          fileExtension = filename.substring(indexDot + 1);
          contentType = BEANS.get(MailHelper.class).getContentTypeForExtension(fileExtension);
          if (contentType == null) {
            contentType = "application/octet-stream";
          }
        }
      }
    }

    m_contentType = contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new SharedByteArrayInputStream(m_binaryResource.getContent(), 0, m_binaryResource.getContentLength());
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new IOException("cannot do this");
  }

  @Override
  public String getContentType() {
    return m_contentType;
  }

  @Override
  public String getName() {
    return m_binaryResource.getFilename();
  }
}
