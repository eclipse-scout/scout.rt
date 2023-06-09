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
import java.util.Collections;
import java.util.Set;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports the following servlet init-params:
 * <ul>
 * <li><b>min_size:</b> minimum size in bytes that is compressed for GET or POST requests (default value =
 * <code>256</code>)
 * <li><b>content_types:</b> list of content types of the response that will be compressed (default value =
 * <code>text/html,text/css,text/xml,text/plain,application/json,application/javascript,image/svg+xml,text/vcard</code>)
 * <li><b>enable_empty_content_type_logging:</b> enables logging of empty content type of the response. (default value =
 * <code>true</code>)
 * </ul>
 */
public class GzipServletFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(GzipServletFilter.class);

  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String GZIP = "gzip";
  public static final String CONTENT_TYPES = "text/html,text/css,text/xml,text/plain,application/json,application/javascript,image/svg+xml,text/vcard";

  private int m_minSize;
  private Set<String> m_contentTypes;
  private boolean m_enableEmptyContentTypeLogging;

  @Override
  public void init(FilterConfig config) throws ServletException {
    // read config
    m_minSize = Integer.parseInt(ObjectUtility.nvl(config.getInitParameter("min_size"), "256"));
    m_contentTypes = CollectionUtility.hashSet(StringUtility.split(ObjectUtility.nvl(config.getInitParameter("content_types"), CONTENT_TYPES), ","));
    m_enableEmptyContentTypeLogging = Boolean.parseBoolean(ObjectUtility.nvl(config.getInitParameter("enable_empty_content_type_logging"), "true"));
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

    if (m_minSize >= 0 && requestAcceptsGzipEncoding(req)) {
      resp = new GzipServletResponseWrapper(resp, req, m_minSize, Collections.unmodifiableSet(m_contentTypes), m_enableEmptyContentTypeLogging);
    }

    chain.doFilter(req, resp);
    if (!req.isAsyncStarted()) {
      if (resp instanceof GzipServletResponseWrapper) {
        GzipServletResponseWrapper gzipResp = (GzipServletResponseWrapper) resp;
        gzipResp.finish();
      }
    } else {
      req.getAsyncContext().addListener(new AsyncListener() {
        @Override
        public void onComplete(AsyncEvent event) throws IOException {
          ServletResponse resp = event.getSuppliedResponse();
          if (resp instanceof GzipServletResponseWrapper) {
            GzipServletResponseWrapper gzipResp = (GzipServletResponseWrapper) resp;
            // This is actually not necessary because the output stream should be completed by now
            // If it is not, finish would log a warning because it is too late to close it
            // -> We just call it to verify it has been completed correctly
            gzipResp.finish();
          }
        }

        @Override
        public void onTimeout(AsyncEvent event) {
        }

        @Override
        public void onError(AsyncEvent event) {
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
        }
      });
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

  @Override
  public void destroy() {
    // no resources to destroy
  }
}
