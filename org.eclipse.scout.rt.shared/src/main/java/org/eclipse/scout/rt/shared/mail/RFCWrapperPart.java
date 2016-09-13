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
package org.eclipse.scout.rt.shared.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.eclipse.scout.rt.platform.util.Assertions;

public class RFCWrapperPart implements Part {
  private final Part m_rfcPart;
  private final String m_fileName;

  public RFCWrapperPart(Part part, String fileName) {
    m_rfcPart = Assertions.assertNotNull(part);
    m_fileName = Assertions.assertNotNullOrEmpty(fileName);
  }

  @Override
  public String getFileName() throws MessagingException {
    return m_fileName;
  }

  @Override
  public void addHeader(String headerName, String headerValue) throws MessagingException {
    m_rfcPart.addHeader(headerName, headerValue);
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
  public String[] getHeader(String headerName) throws MessagingException {
    return m_rfcPart.getHeader(headerName);
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
  public Enumeration getMatchingHeaders(String[] headerNames) throws MessagingException {
    return m_rfcPart.getMatchingHeaders(headerNames);
  }

  @Override
  public Enumeration getNonMatchingHeaders(String[] headerNames) throws MessagingException {
    return m_rfcPart.getNonMatchingHeaders(headerNames);
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
  public void removeHeader(String headerName) throws MessagingException {
    m_rfcPart.removeHeader(headerName);
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
  public void setHeader(String headerName, String headerValue) throws MessagingException {
    m_rfcPart.setHeader(headerName, headerValue);
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
