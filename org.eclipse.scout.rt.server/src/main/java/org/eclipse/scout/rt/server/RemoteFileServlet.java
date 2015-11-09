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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
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
  private static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final String ETAG = "ETag"; //$NON-NLS-1$

  private String m_folder;

  public RemoteFileServlet() {
  }

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

  protected void handleHttpRequest(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
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
      if (("" + ex.toString()).indexOf("Connection reset by peer: socket write error") >= 0) {
        // ignore it
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
    RemoteFile remoteFile = rfs.getRemoteFileHeader(spec);
    if (!remoteFile.exists()) {
      return false;
    }

    long lastModified = remoteFile.getLastModified();
    int contentLength = remoteFile.getContentLength();
    if (setResponseParameters(req, resp, resourcePath, remoteFile.getContentType(), lastModified, contentLength) == HttpServletResponse.SC_NOT_MODIFIED) {
      return true;
    }
    rfs.streamRemoteFile(remoteFile, resp.getOutputStream());
    return true;
  }

  protected int setResponseParameters(final HttpServletRequest req, final HttpServletResponse resp, final String resourcePath, String contentType, long lastModified, int contentLength) {
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
