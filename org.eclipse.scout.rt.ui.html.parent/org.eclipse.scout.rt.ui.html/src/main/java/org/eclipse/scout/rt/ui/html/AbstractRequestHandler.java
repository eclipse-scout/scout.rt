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

/**
 * base class for resource handlers used in {@link AbstractScoutAppServlet}
 */
public abstract class AbstractRequestHandler {

  private final AbstractScoutAppServlet m_servlet;
  private final HttpServletRequest m_req;
  private final HttpServletResponse m_resp;
  private final String m_pathInfo;

  protected AbstractRequestHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    m_servlet = servlet;
    m_req = req;
    m_resp = resp;
    m_pathInfo = pathInfo;
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

  /**
   * @return true if the handler consumed the request by answering to the {@link HttpServletResponse}, return false if
   *         the request was not consumed
   */
  public abstract boolean handle() throws ServletException, IOException;
}
