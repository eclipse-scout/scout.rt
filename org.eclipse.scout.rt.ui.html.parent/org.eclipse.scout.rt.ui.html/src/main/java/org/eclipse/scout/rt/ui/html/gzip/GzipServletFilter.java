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
package org.eclipse.scout.rt.ui.html.gzip;

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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.UiHints;

/**
 * Supports the servlet init-params
 * <p>
 * get_min_size = minimum size that is compressed, default {@value #DEFAULT_GET_MIN_SIZE} = 64
 * <p>
 * post_min_size = minimum size that is compressed, default {@link #DEFAULT_POST_MIN_SIZE} = 64
 * <p>
 * get_pattern = regex of pathInfo that is compressed, default {@link #DEFAULT_GET_PATTERN} = '*(html|css|js)'
 * <p>
 * post_pattern = regex of pathInfo that is compressed, default {@link #DEFAULT_POST_PATTERN} = '/json'
 */
public class GzipServletFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(GzipServletFilter.class);

  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String GZIP = "gzip";

  public static final int DEFAULT_GET_MIN_SIZE = 64;
  public static final int DEFAULT_POST_MIN_SIZE = 64;

  public static final Pattern DEFAULT_GET_PATTERN = Pattern.compile(".*\\.(html|css|js)", Pattern.CASE_INSENSITIVE);
  public static final Pattern DEFAULT_POST_PATTERN = Pattern.compile(".*/json", Pattern.CASE_INSENSITIVE);

  private int m_getMinSize = DEFAULT_GET_MIN_SIZE;
  private int m_postMinSize = DEFAULT_POST_MIN_SIZE;

  private Pattern m_getPattern = DEFAULT_GET_PATTERN;
  private Pattern m_postPattern = DEFAULT_POST_PATTERN;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String s;

    s = filterConfig.getInitParameter("get_min_size");
    if (s != null) {
      m_getMinSize = Integer.parseInt(s);
    }

    s = filterConfig.getInitParameter("post_min_size");
    if (s != null) {
      m_getMinSize = Integer.parseInt(s);
    }

    s = filterConfig.getInitParameter("get_pattern");
    if (s != null) {
      m_getPattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
    }

    s = filterConfig.getInitParameter("post_pattern");
    if (s != null) {
      m_postPattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
    }
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
    if (!UiHints.isCompressHint(req)) {
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

  @Override
  public void destroy() {
  }
}
