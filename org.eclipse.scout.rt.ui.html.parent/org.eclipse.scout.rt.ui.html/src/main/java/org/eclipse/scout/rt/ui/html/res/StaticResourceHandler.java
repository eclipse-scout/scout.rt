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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractRequestHandler;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;
import org.eclipse.scout.rt.ui.html.StreamUtil;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheInfo;

/**
 * Serve static files such as PNG, JPG, PDF, DOCX etc. as a servlet resource using caches.
 * <p>
 * HTML is handled by {@link HtmlFileHandler}
 * <p>
 * CSS and JS is handled by {@link ScriptFileHandler}
 */
public class StaticResourceHandler extends AbstractRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(StaticResourceHandler.class);
  private static int MAX_AGE_4_HOURS = 4 * 3600;

  public StaticResourceHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    super(servlet, req, resp, pathInfo);
  }

  @Override
  public boolean handle() throws ServletException, IOException {
    URL url = getServlet().getResourceLocator().getWebContentResource(getPathInfo());
    if (url == null) {
      return false;
    }

    LOG.info("processing static resource: " + getPathInfo() + " using " + url);

    //check cache state
    URLConnection connection = url.openConnection();
    HttpCacheInfo info = new HttpCacheInfo(connection.getContentLength(), connection.getLastModified(), MAX_AGE_4_HOURS);
    if (HttpServletResponse.SC_NOT_MODIFIED == getServlet().getHttpCacheControl().enableCache(getHttpServletRequest(), getHttpServletResponse(), info)) {
      getHttpServletResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }

    byte[] content = StreamUtil.readResource(url);
    getHttpServletResponse().setContentLength(content.length);

    //Prefer mime type mapping from container
    String path = url.getPath();
    int lastSlash = path.lastIndexOf('/');
    int lastDot = path.lastIndexOf('.');
    String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    String fileExtension = lastDot >= 0 ? path.substring(lastDot + 1) : path;

    String contentType = getServlet().getServletContext().getMimeType(fileName);
    if (contentType == null) {
      contentType = getMsOfficeMimeTypes(fileExtension);
    }
    if (contentType == null) {
      contentType = FileUtility.getContentTypeForExtension(fileExtension);
    }
    if (contentType == null) {
      LOG.warn("Could not determine content type of file " + path);
    }
    else {
      getHttpServletResponse().setContentType(contentType);
    }

    getHttpServletResponse().getOutputStream().write(content);
    return true;
  }

  /**
   * TODO AWE: (scout) In org.eclipse.scout.commons.FileUtility hinzufügen
   * see: http://stackoverflow.com/questions/4212861/what-is-a-correct-mime-type-for-docx-pptx-etc
   */
  private static final Map<String, String> EXT_TO_MIME_TYPE_MAP = new HashMap<>();

  static {
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    EXT_TO_MIME_TYPE_MAP.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
    EXT_TO_MIME_TYPE_MAP.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
    EXT_TO_MIME_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
    EXT_TO_MIME_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    EXT_TO_MIME_TYPE_MAP.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
    EXT_TO_MIME_TYPE_MAP.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
  }

  private String getMsOfficeMimeTypes(String fileExtension) {
    return EXT_TO_MIME_TYPE_MAP.get(fileExtension.toLowerCase());
  }

}
