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
package org.eclipse.scout.rt.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Init parameters for WAR resources<br>
 * war-path: Path to resource within war file. Normally starting with /WEB-INF
 */
public class ResourceServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final String ETAG = "ETag"; //$NON-NLS-1$

  private String m_warPath;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    m_warPath = parseWarPath(config.getInitParameter("war-path"));
  }

  protected String parseWarPath(String value) throws ServletException {
    if (value != null && value.endsWith("/")) {
      return value.substring(0, value.length() - 1);
    }
    if (!StringUtility.hasText(value)) {
      throw new ServletException("Missing init parameters. Set 'war-path' parameter.");
    }
    return value;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String uri = req.getRequestURI();
    int lastSlashPos = uri.lastIndexOf('/');
    String lastSegment = null;
    if (lastSlashPos >= 0 && uri.length() > lastSlashPos) {
      lastSegment = uri.substring(lastSlashPos + 1);
    }
    if ((lastSegment != null && lastSegment.contains(".")) || uri.endsWith("/")) {
      if (!writeStaticResource(req, res)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    }
    else {
      res.sendRedirect(req.getRequestURI() + "/");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    if (!writeStaticResource(req, res)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private boolean writeStaticResource(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || pathInfo.endsWith("/") || pathInfo.equals("")) {
      pathInfo = "/index.html";
    }
    URL url = null;
    String contentType = null;
    if (m_warPath != null) {
      ServletContext servletContext = getServletContext();
      String resourcePath = m_warPath + pathInfo;
      url = servletContext.getResource(resourcePath);
      contentType = servletContext.getMimeType(resourcePath);
    }
    //
    if (url == null) {
      return false;
    }
    //
    long lastModified;
    int contentLength;
    URLConnection connection = url.openConnection();
    lastModified = connection.getLastModified();
    contentLength = connection.getContentLength();
    if (contentType == null) {
      String[] a = pathInfo.split("[.]");
      contentType = FileUtility.getContentTypeForExtension(a[a.length - 1]);
    }
    InputStream is = null;
    try {
      if (setResponseParameters(req, resp, contentType, lastModified, contentLength) == HttpServletResponse.SC_NOT_MODIFIED) {
        return true;
      }
      is = connection.getInputStream();
      OutputStream os = resp.getOutputStream();
      byte[] buffer = new byte[8192];
      int bytesRead = is.read(buffer);
      int writtenContentLength = 0;
      while (bytesRead != -1) {
        os.write(buffer, 0, bytesRead);
        writtenContentLength += bytesRead;
        bytesRead = is.read(buffer);
      }
      if (contentLength == -1 || contentLength != writtenContentLength) {
        resp.setContentLength(writtenContentLength);
      }

      return true;
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }

  protected int setResponseParameters(final HttpServletRequest req, final HttpServletResponse resp, String contentType, long lastModified, int contentLength) {
    String etag = null;
    if (lastModified != -1 && contentLength != -1) {
      etag = "W/\"" + contentLength + "-" + lastModified + "\""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    // Check for cache revalidation.
    // We should prefer ETag validation as the guarantees are stronger and all
    // HTTP 1.1 clients should be using it
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return HttpServletResponse.SC_NOT_MODIFIED;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the
      // fidelity
      // of the IMS header generally doesn't include milli-seconds
      if (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + 999)) {
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return HttpServletResponse.SC_NOT_MODIFIED;
      }
    }

    // return the full contents regularly
    if (contentLength != -1) {
      resp.setContentLength(contentLength);
    }

    if (contentType != null) {
      resp.setContentType(contentType);
    }

    if (lastModified > 0) {
      resp.setDateHeader(LAST_MODIFIED, lastModified);
    }

    if (etag != null) {
      resp.setHeader(ETAG, etag);
    }

    return HttpServletResponse.SC_ACCEPTED;
  }

}
