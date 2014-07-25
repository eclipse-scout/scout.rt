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
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.server.commons.internal.FilterChainImpl;
import org.eclipse.scout.rt.server.commons.internal.FilterConfigImpl;
import org.eclipse.scout.rt.server.commons.internal.ServletFilterExtensionPoint;

/**
 * DO NOT extend.
 */
public class ServletFilterDelegate {

  public interface IServiceCallback {
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

    ServletContext getServletContext();
  }

  public ServletFilterDelegate() {
  }

  public void delegateServiceMethod(ServletRequest req, ServletResponse res, IServiceCallback callback) throws ServletException, IOException {
    String servletPath = ((HttpServletRequest) req).getServletPath();
    if (servletPath == null) {
      servletPath = "/";
    }
    else if (!servletPath.startsWith("/")) {
      servletPath = "/" + servletPath;
    }
    ArrayList<Filter> filterList = new ArrayList<Filter>();
    //
    ServletContext servletContext = callback.getServletContext();
    for (FilterConfigImpl ref : ServletFilterExtensionPoint.getExtensions()) {
      if (ref.isFiltering(servletPath)) {
        Filter f = ref.getFilter(servletContext);
        if (f != null) {
          filterList.add(f);
        }
      }
    }
    new FilterChainImpl(filterList, callback).doFilter(req, res);
  }
}
