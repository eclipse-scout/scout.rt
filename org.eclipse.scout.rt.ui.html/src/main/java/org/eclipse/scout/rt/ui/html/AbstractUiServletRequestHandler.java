/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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

public abstract class AbstractUiServletRequestHandler implements IUiServletRequestHandler {

  @Override
  public boolean handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String httpMethod = req.getMethod();
    switch (httpMethod) {
      case "GET":
        return handleGet(req, resp);
      case "POST":
        return handlePost(req, resp);
      case "PUT":
        return handlePut(req, resp);
      case "DELETE":
        return handleDelete(req, resp);
      default:
        return false;
    }
  }

  /**
   * Convenience method for HTTP method GET.
   *
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  protected boolean handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  /**
   * Convenience method for HTTP method POST.
   *
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  protected boolean handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  /**
   * Convenience method for HTTP method PUT.
   *
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  protected boolean handlePut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  /**
   * Convenience method for HTTP method DELETE.
   *
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  protected boolean handleDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }
}
