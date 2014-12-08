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
import org.eclipse.scout.rt.ui.html.ScoutAppHints;

public class GZIPServletFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(GZIPServletFilter.class);

  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String GZIP = "gzip";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest req0, ServletResponse resp0, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) req0;
    HttpServletResponse resp = (HttpServletResponse) resp0;

    if (requestHasGZIPEncoding(req)) {
      LOG.info("GZIP request: " + req.getPathInfo());
      req = new GZIPServletRequestWrapper(req);
    }
    if (requestAcceptsGZIPEncoding(req) && ScoutAppHints.isCompressHint(req)) {
      LOG.info("GZIP response: " + req.getPathInfo());
      resp = new GZIPServletResponseWrapper(resp);
    }

    chain.doFilter(req, resp);

    if (resp instanceof GZIPServletResponseWrapper) {
      ((GZIPServletResponseWrapper) resp).finish();
    }
  }

  private boolean requestHasGZIPEncoding(HttpServletRequest req) {
    String h = req.getHeader(CONTENT_ENCODING);
    return h != null && h.contains(GZIP);
  }

  private boolean requestAcceptsGZIPEncoding(HttpServletRequest req) {
    String h = req.getHeader(ACCEPT_ENCODING);
    return h != null && h.contains(GZIP);
  }

  @Override
  public void destroy() {
  }
}
