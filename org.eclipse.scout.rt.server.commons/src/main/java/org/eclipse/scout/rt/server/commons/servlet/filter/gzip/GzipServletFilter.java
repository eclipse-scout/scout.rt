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
package org.eclipse.scout.rt.server.commons.servlet.filter.gzip;

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

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports the following servlet init-params:
 * <ul>
 * <li><b>get_min_size:</b> minimum size in bytes that is compressed for GET requests (default value = <code>256</code>)
 * <li><b>post_min_size:</b> minimum size in bytes that is compressed for POST requests (default value =
 * <code>256</code>)
 * <li><b>get_pattern:</b> regex of pathInfo that is compressed for GET requests (default value =
 * <code>.*\.(html|css|js|json|txt)</code>)
 * <li><b>post_pattern:</b> regex of pathInfo that is compressed for POST requests (default value =
 * <code>.{@literal *}/json</code>)
 * </ul>
 */
public class GzipServletFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(GzipServletFilter.class);

  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String GZIP = "gzip";

  private int m_getMinSize;
  private int m_postMinSize;
  private Pattern m_getPattern;
  private Pattern m_postPattern;

  @Override
  public void init(FilterConfig config) throws ServletException {
    // read config
    m_getMinSize = Integer.parseInt(StringUtility.nvl(config.getInitParameter("get_min_size"), "256"));
    m_postMinSize = Integer.parseInt(StringUtility.nvl(config.getInitParameter("post_min_size"), "256"));
    m_getPattern = Pattern.compile(StringUtility.nvl(config.getInitParameter("get_pattern"), ".*\\.(html|css|js|json|txt)"), Pattern.CASE_INSENSITIVE);
    m_postPattern = Pattern.compile(StringUtility.nvl(config.getInitParameter("post_pattern"), ".*/json"), Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void doFilter(ServletRequest req0, ServletResponse resp0, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) req0;
    HttpServletResponse resp = (HttpServletResponse) resp0;

    if (requestHasGzipEncoding(req)) {
      GzipServletRequestWrapper gzipReq = new GzipServletRequestWrapper(req);
      req = gzipReq;
      if (LOG.isDebugEnabled()) {
        LOG.debug("GZIP request[size {}%, compressed: {}, uncompressed: {}]: {}",
            gzipReq.getCompressedLength() * 100 / gzipReq.getUncompressedLength(),
            gzipReq.getCompressedLength(),
            gzipReq.getUncompressedLength(),
            req.getPathInfo());
      }
    }
    if (requestAcceptsGzipEncoding(req) && supportsGzipEncoding(req)) {
      resp = new GzipServletResponseWrapper(resp);
    }

    chain.doFilter(req, resp);

    if (resp instanceof GzipServletResponseWrapper) {
      GzipServletResponseWrapper gzipResp = (GzipServletResponseWrapper) resp;
      boolean compressed = gzipResp.finish(minimumLengthToCompress(req));
      if (compressed && LOG.isDebugEnabled()) {
        LOG.debug("GZIP response[size {}%, uncompressed: {}, compressed: {}]: {}",
            gzipResp.getCompressedLength() * 100 / gzipResp.getUncompressedLength(),
            gzipResp.getUncompressedLength(),
            gzipResp.getCompressedLength(),
            req.getPathInfo());
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
    if (!UrlHints.isCompressHint(req)) {
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
    // no resources to destroy
  }
}
