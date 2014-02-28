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
package org.eclipse.scout.rt.server.commons.servletfilter;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * In order to have servlet filters inside equinox use this baseclass instead of {@link HttpServlet}.<br>
 * See extension point org.eclipse.scout.http.servletfilter.filters in plugin
 * org.eclipse.scout.http.servletfilter<br>
 * For debugging use system property javax.servlet.filter.debug=true<br>
 * <p>
 * This is a temporary solution until bug <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=128068"
 * >https://bugs.eclipse.org/bugs/show_bug.cgi?id=128068</a> is closed.
 */
public class HttpServletEx extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    new ServletFilterDelegate().delegateServiceMethod(req, res, new ServletFilterDelegate.IServiceCallback() {
      @Override
      public void service(ServletRequest reqInner, ServletResponse resInner) throws ServletException, IOException {
        HttpServletEx.super.service(reqInner, resInner);
      }

      @Override
      public ServletContext getServletContext() {
        return HttpServletEx.this.getServletContext();
      }
    });
  }

}
