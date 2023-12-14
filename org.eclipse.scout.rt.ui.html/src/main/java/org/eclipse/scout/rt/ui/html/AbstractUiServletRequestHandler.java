/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
