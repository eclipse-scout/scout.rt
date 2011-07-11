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
package org.eclipse.scout.http.servletfilter.internal;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.core.runtime.Status;
import org.eclipse.scout.http.servletfilter.ServletFilterDelegate;

public class FilterChainImpl implements FilterChain {
  private List<Filter> m_filters;
  private ServletFilterDelegate.IServiceCallback m_callback;

  public FilterChainImpl(List<Filter> filters, ServletFilterDelegate.IServiceCallback callback) {
    m_filters = filters;
    m_callback = callback;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
    if (m_filters.size() > 0) {
      Filter nextFilter = m_filters.remove(0);
      if (Activator.DEBUG) Activator.getDefault().getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID, "ServletFilterChain: doFilter " + nextFilter.getClass().getSimpleName()));
      nextFilter.doFilter(req, res, this);
    }
    else {
      m_callback.service(req, res);
    }
  }

}
