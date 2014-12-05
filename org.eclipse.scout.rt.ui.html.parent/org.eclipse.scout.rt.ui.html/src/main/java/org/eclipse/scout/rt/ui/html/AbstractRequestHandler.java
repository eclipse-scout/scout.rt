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
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;

/**
 * base class for resource handlers used in {@link AbstractScoutAppServlet}
 */
public abstract class AbstractRequestHandler {
  public static final String SESSION_ATTR_DEBUG_ENABLED = AbstractRequestHandler.class.getSimpleName() + "debug";

  private final AbstractScoutAppServlet m_servlet;
  private final HttpServletRequest m_req;
  private final HttpServletResponse m_resp;
  private final String m_pathInfo;
  private final boolean m_debug;

  protected AbstractRequestHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    m_servlet = servlet;
    m_req = req;
    m_resp = resp;
    m_pathInfo = pathInfo;
    m_debug = calculateDebugEnabled(req);
  }

  protected boolean calculateDebugEnabled(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return false;
    }
    Boolean sessionFlag = (Boolean) session.getAttribute(SESSION_ATTR_DEBUG_ENABLED);
    if (sessionFlag != null) {
      return sessionFlag.booleanValue();
    }
    if (Platform.inDevelopmentMode()) {
      return true;
    }
    return false;
  }

  public AbstractScoutAppServlet getServlet() {
    return m_servlet;
  }

  public HttpServletRequest getHttpServletRequest() {
    return m_req;
  }

  public HttpServletResponse getHttpServletResponse() {
    return m_resp;
  }

  public String getPathInfo() {
    return m_pathInfo;
  }

  public boolean isDebug() {
    return m_debug;
  }

  /**
   * @return true if the handler consumed the request by answering to the {@link HttpServletResponse}, return false if
   *         the request was not consumed
   */
  public abstract boolean handle() throws ServletException, IOException;
}
