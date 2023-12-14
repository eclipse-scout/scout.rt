/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.gzip;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
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
public class LegacyGzipServletFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(LegacyGzipServletFilter.class);

  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String GZIP = "gzip";
  public static final String CONTENT_TYPES = "text/html,text/css,text/xml,text/plain,application/json,application/javascript,image/svg+xml,text/vcard";

  private int m_getMinSize;
  private int m_postMinSize;
  private Set<String> m_contentTypes;

  @Override
  public void init(FilterConfig config) throws ServletException {
    // read config
    m_getMinSize = Integer.parseInt(ObjectUtility.nvl(config.getInitParameter("get_min_size"), "256"));
    m_postMinSize = Integer.parseInt(ObjectUtility.nvl(config.getInitParameter("post_min_size"), "256"));
    m_contentTypes = CollectionUtility.hashSet(StringUtility.split(ObjectUtility.nvl(config.getInitParameter("content_types"), CONTENT_TYPES), ","));
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
    if (requestAcceptsGzipEncoding(req)) {
      resp = new LegacyGzipServletResponseWrapper(resp);
    }

    chain.doFilter(req, resp);

    if (resp instanceof LegacyGzipServletResponseWrapper) {
      LegacyGzipServletResponseWrapper gzipResp = (LegacyGzipServletResponseWrapper) resp;
      int minLength = minimumLengthToCompress(req);
      if (!responseNeedsGzipEncoding(req, resp)) {
        // Disable compression
        minLength = -1;
      }
      boolean compressed = gzipResp.finish(minLength);
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

  protected boolean responseNeedsGzipEncoding(HttpServletRequest req, HttpServletResponse resp) {
    if (!UrlHints.isCompressHint(req)) {
      return false;
    }
    String contentType = resp.getContentType();
    if (contentType == null) {
      return false;
    }
    // Content type may contain the charset parameter separated by ; -> remove it
    contentType = contentType.split(";")[0];
    if (m_contentTypes.contains(contentType)) {
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
