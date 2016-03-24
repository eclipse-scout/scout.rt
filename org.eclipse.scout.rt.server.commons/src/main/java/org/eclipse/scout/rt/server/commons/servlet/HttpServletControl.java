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
package org.eclipse.scout.rt.server.commons.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Add default (security) handling to servlets
 * <p>
 * Make sure to call {@link #doDefaults(HttpServletRequest, HttpServletResponse)} in every servlet at the beginning of
 * each doGet and doPost
 *
 * @since 5.1
 */
@ApplicationScoped
public class HttpServletControl {
  public static final String HTTP_HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
  public static final String SAMEORIGIN = "SAMEORIGIN";

  /**
   * Every servlet should call this method to make sure the defaults are applied
   * <p>
   * This includes setting default security response headers, parsing default request attributes etc.
   */
  public void doDefaults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    parseRequest(servlet, req, resp);
    setResponseHeaders(servlet, req, resp);
  }

  protected void parseRequest(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    UrlHints.updateHints(req);
  }

  protected void setResponseHeaders(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader(HTTP_HEADER_X_FRAME_OPTIONS, SAMEORIGIN);
  }
}
