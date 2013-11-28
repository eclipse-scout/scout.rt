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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceManagerImpl;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.internal.service.UrlParameters;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.swt.widgets.Display;

public class RwtScoutDownloadHandler implements ServiceHandler {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDownloadHandler.class);

  private File m_file;
  private URI m_bundleURI;
  private final String m_fileName;
  private final String m_contentType;
  private final String m_requestId;
  private RwtScoutDownloadDialog m_sdd;

  private RwtScoutDownloadHandler(String id, String contentType, String fileName) {
    m_requestId = id;
    m_fileName = fileName;
    m_contentType = contentType;
  }

  public RwtScoutDownloadHandler(String id, File file, String contentType, String fileName) {
    this(id, contentType, fileName);
    m_file = file;
  }

  public RwtScoutDownloadHandler(String id, URI bundleURI, String contentType, String fileName) {
    this(id, contentType, fileName);
    m_bundleURI = bundleURI;
  }

  public void startDownload() {
    RWT.getServiceManager().unregisterServiceHandler(m_requestId);
    if (m_sdd != null) {
      m_sdd.close();
      m_sdd = null;
    }

    RWT.getServiceManager().registerServiceHandler(m_requestId, this);
    m_sdd = new RwtScoutDownloadDialog(null, getURL());
    m_sdd.open();
  }

  protected String getURL() {
    /* inline implementation of RWT.getServiceManager().getServiceHandlerUrl(m_requestId) and remove the following 3 lines:
     *  HttpServletRequest request = ContextProvider.getRequest();
     *  url.append( request.getContextPath() );
     *  url.append( request.getServletPath() );
     * since setting context and servlet path are wrong if the server is behind a (reverse) proxy.
     * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=388915 for more details.
     */

    // >> inlined implementation start
    StringBuilder url = new StringBuilder();
    url.append("?");
    url.append(ServiceManagerImpl.REQUEST_PARAM);
    url.append("=");
    try {
      url.append(URLEncoder.encode(m_requestId, "UTF-8").replace("+", "%20"));
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    String connectionId = ((UISessionImpl) ContextProvider.getUISession()).getConnectionId();
    if (connectionId != null) {
      url.append('&');
      url.append(UrlParameters.PARAM_CONNECTION_ID);
      url.append('=');
      url.append(connectionId);
    }
    String decodedURL = ContextProvider.getResponse().encodeURL(url.toString());
    // << inlined implementation end

    String encodedURL = RWT.getResponse().encodeURL(decodedURL);
    return encodedURL;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try {
      writeResponse(response);
    }
    finally {
      if (m_sdd != null) {
        Display display = m_sdd.getShell().getDisplay();
        display.asyncExec(new Runnable() {

          @Override
          public void run() {
            m_sdd.close();
            m_sdd = null;
          }
        });
      }
      RWT.getServiceManager().unregisterServiceHandler(m_requestId);
    }
  }

  protected void writeResponse(HttpServletResponse response) throws IOException {
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
        LOG.error("IOEception while writing file.", e);
      }
      finally {
        srcStream.close();
      }
    }
  }

}
