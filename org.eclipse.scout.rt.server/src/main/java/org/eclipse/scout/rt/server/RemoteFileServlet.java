/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet facade to provide remote files in web context<br>
 * <p>
 * Init parameters:<br>
 * folder: folder inside external file location
 */
public class RemoteFileServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(RemoteFileServlet.class);

  private String m_folder;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    m_folder = parseFolderParam(config.getInitParameter("folder"));
  }

  protected String parseFolderParam(String value) {
    if (!StringUtility.hasText(value)) {
      return "";
    }

    value = value.replaceAll("\\\\", "/"); //$NON-NLS-1$
    while (value.startsWith("/")) {
      value = value.substring(1);
    }
    while (value.endsWith("/")) {
      value = value.substring(0, value.lastIndexOf('/'));
    }
    return '/' + value;
  }

  protected String getFolder() {
    return m_folder;
  }

  protected void setFolder(String folder) {
    m_folder = folder;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    handleHttpRequest(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    handleHttpRequest(req, res);
  }

  protected void handleHttpRequest(final HttpServletRequest req, final HttpServletResponse res) {
    BEANS.get(HttpServletControl.class).doDefaults(this, req, res);

    String pathInfo = extractPathInfo(req);

    // will try to get all files in fileList.
    List<String> fileList = Arrays.asList(pathInfo);

    // @rn aho, 15.7.2008: iff a directory is requested (e.g. root directory
    // "/") then try index.*-files.
    if (StringUtility.isNullOrEmpty(pathInfo) || pathInfo.replaceAll("\\\\", "/").endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
      String prefix = "/"; //$NON-NLS-1$
      if (pathInfo != null && pathInfo.replaceAll("\\\\", "/").endsWith("/")) {
        prefix = pathInfo.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      fileList = Arrays.asList(
          prefix + "index.html", //$NON-NLS-1$
          prefix + "index.htm", //$NON-NLS-1$
          prefix + "default.html", //$NON-NLS-1$
          prefix + "default.htm", //$NON-NLS-1$
          prefix + "index.jsp", //$NON-NLS-1$
          prefix + "index.php" //$NON-NLS-1$
      );
    }
    //
    try {
      final List<String> fileListParam = fileList;
      Iterator<String> pathIter = fileListParam.iterator();
      boolean success = false;
      while (!success && pathIter.hasNext()) {
        success = writeResource(req, res, pathIter.next());
      }
      if (!success) {
        res.sendError(HttpServletResponse.SC_NOT_FOUND, pathInfo);
      }
    }
    catch (Exception ex) {
      if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(ex)) {
        // Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
        LOG.debug("Connection error detected: exception class={}, message={}.", ex.getClass().getSimpleName(), ex.getMessage(), ex);
      }
      else {
        LOG.warn("Failed to get remotefile {}.", pathInfo, ex);
        // cannot sendError(..) here, since OutputStream maybe is already
        // committed.
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  /**
   * Returns the path information from the request.
   */
  protected String extractPathInfo(final HttpServletRequest request) {
    return request.getPathInfo();
  }

  /**
   * @return the remote file service class to use to load the remote file.
   */
  protected Class<? extends IRemoteFileService> getConfiguredRemoteFileServiceClass() {
    return IRemoteFileService.class;
  }

  private boolean writeResource(final HttpServletRequest req, final HttpServletResponse resp, final String resourcePath) throws IOException {
    IRemoteFileService rfs = BEANS.get(getConfiguredRemoteFileServiceClass());
    RemoteFile spec = new RemoteFile((resourcePath == null) ? null : StringUtility.join("", m_folder, resourcePath), -1);
    RemoteFile remoteFile = rfs.getRemoteFile(spec);
    if (!remoteFile.exists()) {
      return false;
    }

    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey(resourcePath), remoteFile.toBinaryResource());
    if (BEANS.get(HttpCacheControl.class).checkAndSetCacheHeaders(req, resp, obj)) {
      return true;
    }
    rfs.streamRemoteFile(remoteFile, resp.getOutputStream());
    return true;
  }

}
