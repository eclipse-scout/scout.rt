package org.eclipse.scout.commons.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.util.SharedByteArrayInputStream;

import org.eclipse.scout.commons.resource.BinaryResource;

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
          contentType = MailUtility.getContentTypeForExtension(fileExtension);
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
