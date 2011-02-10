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
package org.eclipse.scout.rt.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.http.servletfilter.HttpServletEx;
import org.osgi.framework.Bundle;

/**
 * Init parameters for WAR resources<br>
 * war-path: Path to resource within war file. Normally starting with /WEB-INF
 * <p>
 * Init parameters for osgi bundle resources<br>
 * bundle-name: symbolic name of bundle with resources<br>
 * bundle-path: Path to resource within bundle. Normally starting with /resources
 * <p>
 * Legacy parameters<br>
 * base-name: same as war-path
 */
public class ResourceServlet extends HttpServletEx {

  private static final long serialVersionUID = 1L;
  private static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final String ETAG = "ETag"; //$NON-NLS-1$

  private String m_warPath;
  private String m_bundleName;
  private String m_bundlePath;

  public ResourceServlet() {
  }

  public ResourceServlet(String internalName) {
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    m_warPath = config.getInitParameter("base-name"); //$NON-NLS-1$, legacy
    if (m_warPath == null) {
      m_warPath = config.getInitParameter("war-path"); //$NON-NLS-1$
    }
    if (m_warPath == null || m_warPath.trim().length() == 0) m_warPath = null;
    if (m_warPath != null && m_warPath.endsWith("/")) m_warPath = m_warPath.substring(0, m_warPath.length() - 1);
    //
    m_bundleName = config.getInitParameter("bundle-name"); //$NON-NLS-1$
    if (m_bundleName == null || m_bundleName.trim().length() == 0) m_bundleName = null;
    //
    m_bundlePath = config.getInitParameter("bundle-path"); //$NON-NLS-1$
    if (m_bundlePath == null || m_bundlePath.trim().length() == 0) m_bundlePath = null;
    if (m_bundlePath != null && m_bundlePath.endsWith("/")) m_bundlePath = m_bundlePath.substring(0, m_bundlePath.length() - 1);
    //
    // check config
    if (m_warPath != null) {
      // ok
    }
    else if (m_bundleName != null && m_bundlePath != null) {
      // ok
    }
    else {
      throw new ServletException("Missing init parameters. Set either 'war-path' or 'bundle-name','bundle-path'");
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || pathInfo.equals("")) {
      res.sendRedirect(req.getRequestURI() + "/");
    }
    else {
      if (!writeStaticResource(req, res)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
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
    if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("")) {
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
    else if (m_bundleName != null && m_bundlePath != null) {
      String resourcePath = m_bundlePath + pathInfo;
      Bundle bundle = Platform.getBundle(m_bundleName);
      if (bundle != null) {
        url = bundle.getResource(resourcePath);
      }
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
      if (contentLength == -1 || contentLength != writtenContentLength) resp.setContentLength(writtenContentLength);

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
    if (lastModified != -1 && contentLength != -1) etag = "W/\"" + contentLength + "-" + lastModified + "\""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

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
    if (contentLength != -1) resp.setContentLength(contentLength);

    if (contentType != null) resp.setContentType(contentType);

    if (lastModified > 0) resp.setDateHeader(LAST_MODIFIED, lastModified);

    if (etag != null) resp.setHeader(ETAG, etag);

    return HttpServletResponse.SC_ACCEPTED;
  }

}
