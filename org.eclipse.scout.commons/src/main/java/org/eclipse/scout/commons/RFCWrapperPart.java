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
package org.eclipse.scout.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

public class RFCWrapperPart implements Part {
  private final Part m_rfcPart;
  private final String m_fileName;

  public RFCWrapperPart(Part part, String fileName) {
    super();
    if (part != null && fileName != null && StringUtility.hasText(fileName)) {
      m_rfcPart = part;
      m_fileName = fileName;
    }
    else {
      throw new NullPointerException("RFC-Part and fileName should not be null or empty.");
    }
  }

  @Override
  public String getFileName() throws MessagingException {
    return m_fileName;
  }

  @Override
  public void addHeader(String header_name, String header_value) throws MessagingException {
    m_rfcPart.addHeader(header_name, header_value);
  }

  @Override
  public Enumeration getAllHeaders() throws MessagingException {
    return m_rfcPart.getAllHeaders();
  }

  @Override
  public Object getContent() throws IOException, MessagingException {
    return m_rfcPart.getContent();
  }

  @Override
  public String getContentType() throws MessagingException {
    return m_rfcPart.getContentType();
  }

  @Override
  public DataHandler getDataHandler() throws MessagingException {
    return m_rfcPart.getDataHandler();
  }

  @Override
  public String getDescription() throws MessagingException {
    return m_rfcPart.getDescription();
  }

  @Override
  public String getDisposition() throws MessagingException {
    return m_rfcPart.getDisposition();
  }

  @Override
  public String[] getHeader(String header_name) throws MessagingException {
    return m_rfcPart.getHeader(header_name);
  }

  @Override
  public InputStream getInputStream() throws IOException, MessagingException {
    return m_rfcPart.getInputStream();
  }

  @Override
  public int getLineCount() throws MessagingException {
    return m_rfcPart.getLineCount();
  }

  @Override
  public Enumeration getMatchingHeaders(String[] header_names) throws MessagingException {
    return m_rfcPart.getMatchingHeaders(header_names);
  }

  @Override
  public Enumeration getNonMatchingHeaders(String[] header_names) throws MessagingException {
    return m_rfcPart.getNonMatchingHeaders(header_names);
  }

  @Override
  public int getSize() throws MessagingException {
    return m_rfcPart.getSize();
  }

  @Override
  public boolean isMimeType(String mimeType) throws MessagingException {
    return m_rfcPart.isMimeType(mimeType);
  }

  @Override
  public void removeHeader(String header_name) throws MessagingException {
    m_rfcPart.removeHeader(header_name);
  }

  @Override
  public void setContent(Multipart mp) throws MessagingException {
    m_rfcPart.setContent(mp);
  }

  @Override
  public void setContent(Object obj, String type) throws MessagingException {
    m_rfcPart.setContent(obj, type);
  }

  @Override
  public void setDataHandler(DataHandler dh) throws MessagingException {
    m_rfcPart.setDataHandler(dh);
  }

  @Override
  public void setDescription(String description) throws MessagingException {
    m_rfcPart.setDescription(description);
  }

  @Override
  public void setDisposition(String disposition) throws MessagingException {
    m_rfcPart.setDisposition(disposition);
  }

  @Override
  public void setFileName(String filename) throws MessagingException {
    m_rfcPart.setFileName(filename);
  }

  @Override
  public void setHeader(String header_name, String header_value) throws MessagingException {
    m_rfcPart.setHeader(header_name, header_value);
  }

  @Override
  public void setText(String text) throws MessagingException {
    m_rfcPart.setText(text);
  }

  @Override
  public void writeTo(OutputStream os) throws IOException, MessagingException {
    m_rfcPart.writeTo(os);
  }
}
