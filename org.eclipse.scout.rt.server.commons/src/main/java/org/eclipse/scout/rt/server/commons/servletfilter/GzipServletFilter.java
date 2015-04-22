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
package org.eclipse.scout.rt.server.commons.servletfilter;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.GzipFilterGetMinSizeProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.GzipFilterGetPatternProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.GzipFilterPostMinSizeProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.GzipFilterPostPatternProperty;
import org.eclipse.scout.rt.server.commons.config.WebXmlConfigManager;

/**
 * Supports the servlet init-params
 * <p>
 * get_min_size = minimum size that is compressed
 * <p>
 * post_min_size = minimum size that is compressed
 * <p>
 * get_pattern = regex of pathInfo that is compressed
 * <p>
 * post_pattern = regex of pathInfo that is compressed
 */
public class GzipServletFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(GzipServletFilter.class);

  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String GZIP = "gzip";
  public static final String URL_PARAM_COMPRESS_HINT = "compress";
  public static final String SESSION_ATTRIBUTE_COMPRESS_HINT = GzipServletFilter.class.getName() + "#compress";

  private WebXmlConfigManager m_configManager;
  private int m_getMinSize;
  private int m_postMinSize;
  private Pattern m_getPattern;
  private Pattern m_postPattern;

  @Override
  public void init(FilterConfig config) throws ServletException {
    m_configManager = new WebXmlConfigManager(config);

    // read config
    m_getMinSize = m_configManager.getPropertyValue(GzipFilterGetMinSizeProperty.class);
    m_postMinSize = m_configManager.getPropertyValue(GzipFilterPostMinSizeProperty.class);
    m_getPattern = Pattern.compile(m_configManager.getPropertyValue(GzipFilterGetPatternProperty.class), Pattern.CASE_INSENSITIVE);
    m_postPattern = Pattern.compile(m_configManager.getPropertyValue(GzipFilterPostPatternProperty.class), Pattern.CASE_INSENSITIVE);
  }

  protected WebXmlConfigManager getConfigManager() {
    return m_configManager;
  }

  @Override
  public void doFilter(ServletRequest req0, ServletResponse resp0, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) req0;
    HttpServletResponse resp = (HttpServletResponse) resp0;

    if (requestHasGzipEncoding(req)) {
      GzipServletRequestWrapper gzipReq = new GzipServletRequestWrapper(req);
      req = gzipReq;
      if (LOG.isInfoEnabled()) {
        LOG.info("GZIP request[size " + (gzipReq.getCompressedLength() * 100 / gzipReq.getUncompressedLength()) + "%, compressed: " + gzipReq.getCompressedLength() + ", uncompressed: " + gzipReq.getUncompressedLength() + "]: " + req.getPathInfo());
      }
    }
    if (requestAcceptsGzipEncoding(req) && supportsGzipEncoding(req)) {
      resp = new GzipServletResponseWrapper(resp);
    }

    chain.doFilter(req, resp);

    if (resp instanceof GzipServletResponseWrapper) {
      GzipServletResponseWrapper gzipResp = (GzipServletResponseWrapper) resp;
      boolean compressed = gzipResp.finish(minimumLengthToCompress(req));
      if (compressed) {
        if (LOG.isInfoEnabled()) {
          LOG.info("GZIP response[size " + (gzipResp.getCompressedLength() * 100 / gzipResp.getUncompressedLength()) + "%, uncompressed: " + gzipResp.getUncompressedLength() + ", compressed: " + gzipResp.getCompressedLength() + "]: " + req.getPathInfo());
        }
      }
    }
  }

  protected boolean requestHasGzipEncoding(HttpServletRequest req) {
    String h = req.getHeader(CONTENT_ENCODING);
    return h != null && h.contains(GZIP);
  }

  protected boolean requestAcceptsGzipEncoding(HttpServletRequest req) {
    String h = req.getHeader(ACCEPT_ENCODING);
    return h != null && h.contains(GZIP);
  }

  protected boolean supportsGzipEncoding(HttpServletRequest req) {
    if (!isCompressHint(req)) {
      return false;
    }
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return false;
    }
    if ("GET".equals(req.getMethod()) && m_getPattern.matcher(pathInfo).matches()) {
      return true;
    }
    if ("POST".equals(req.getMethod()) && m_postPattern.matcher(pathInfo).matches()) {
      return true;
    }
    return false;
  }

  protected int minimumLengthToCompress(HttpServletRequest req) {
    if ("GET".equals(req.getMethod())) {
      return m_getMinSize;
    }
    if ("POST".equals(req.getMethod())) {
      return m_postMinSize;
    }
    return -1;
  }

  private boolean isCompressHint(HttpServletRequest req) {
    boolean defaultValue = !Platform.get().inDevelopmentMode();
    HttpSession session = req.getSession(false);
    if (session == null) {
      return defaultValue;
    }
    Boolean val = (Boolean) session.getAttribute(SESSION_ATTRIBUTE_COMPRESS_HINT);
    if (val == null) {
      return defaultValue;
    }
    return val.booleanValue();
  }

  @Override
  public void destroy() {
  }
}
