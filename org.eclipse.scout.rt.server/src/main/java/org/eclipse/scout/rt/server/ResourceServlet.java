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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;

/**
 * Init parameters for WAR resources<br>
 * war-path: Path to resource within war file. Normally starting with /WEB-INF
 */
public class ResourceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

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
    BEANS.get(HttpServletControl.class).doDefaults(this, req, res);
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
    BEANS.get(HttpServletControl.class).doDefaults(this, req, res);
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
      contentType = FileUtility.getMimeType(resourcePath);
    }
    //
    if (url == null) {
      return false;
    }

    long lastModified;
    int contentLength;
    URLConnection connection = url.openConnection();
    lastModified = connection.getLastModified();
    contentLength = connection.getContentLength();
    BinaryResource res = BinaryResources.create().withFilename(pathInfo).withContentType(contentType).withLastModified(lastModified).build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey(pathInfo), res);
    if (BEANS.get(HttpCacheControl.class).checkAndSetCacheHeaders(req, resp, pathInfo, obj)) {
      return true;
    }

    try (InputStream is = connection.getInputStream()) {
      @SuppressWarnings("resource")
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
    }
    return true;
  }
}
