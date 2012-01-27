/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.filedownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.swt.widgets.Shell;

public class RwtScoutDownloadHandler implements IServiceHandler {

  private File m_file;
  private URI m_bundleURI;
  private String m_fileName;
  private String m_contentType;
  private String m_requestId;
  private RwtScoutDownloadDialog m_sdd;

  private RwtScoutDownloadHandler(String id, String contentType, String fileName) {
    m_requestId = id.replaceAll("\\s", "%20");
    m_fileName = fileName;
    m_contentType = contentType;
    RWT.getServiceManager().registerServiceHandler(id + hashCode(), this);
  }

  public RwtScoutDownloadHandler(String id, File file, String contentType, String fileName) {
    this(id, contentType, fileName);
    m_file = file;
  }

  public RwtScoutDownloadHandler(String id, URI bundleURI, String contentType, String fileName) {
    this(id, contentType, fileName);
    m_bundleURI = bundleURI;
  }

  public void startDownload(Shell parentShell) {
    m_sdd = new RwtScoutDownloadDialog(parentShell, getURL());
    m_sdd.open();
  }

  public String getURL() {
    StringBuffer url = new StringBuffer();
    url.append(RWT.getRequest().getContextPath());
    url.append(RWT.getRequest().getServletPath());
    url.append("?");
    url.append(IServiceHandler.REQUEST_PARAM);
    url.append("=" + m_requestId + hashCode());

    String encodedURL = RWT.getResponse().encodeURL(url.toString());
    return encodedURL;
  }

  @Override
  public void service() throws IOException, ServletException {
    HttpServletResponse response = RWT.getResponse();
    if (StringUtility.hasText(m_contentType)) {
      response.setContentType(m_contentType);
    }
    else {
      response.setContentType("application/octet-stream");
    }
    String contentDisposition = "attachment; filename=\"" + m_fileName + "\"";
    response.setHeader("Content-Disposition", contentDisposition);

    ServletOutputStream outStream = response.getOutputStream();
    InputStream srcStream = null;

    if (m_file != null) {
      srcStream = new FileInputStream(m_file);
      if (m_file.length() < Integer.MAX_VALUE) {
        response.setContentLength((int) m_file.length());
      }
    }
    else if (m_bundleURI != null) {
      srcStream = m_bundleURI.toURL().openStream();
    }

    if (srcStream != null) {
      try {
        byte[] content = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = srcStream.read(content)) > 0) {
          outStream.write(content, 0, bytesRead);
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      finally {
        srcStream.close();
      }
    }
  }

  public void dispose() {
    if (m_sdd != null) {
      m_sdd.close();
    }
    RWT.getServiceManager().unregisterServiceHandler(m_requestId + hashCode());
  }
}
