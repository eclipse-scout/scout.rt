/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.internal.servletfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.http.servletfilter.ServletFilterDelegate;

/**
 * This filter is registered under the extension point <code>org.eclipse.equinox.http.registry.filters</code> and
 * triggers all registered servlet filters under extension point
 * <code>org.eclipse.scout.http.servletfilter.filters</code>.<br>
 * The reason for this delegation filter is the support of rankings in the scout servlet filters. In case of security
 * filters a ranking is strong requirement to support chainable security filters.
 */
public class DelegateFilter implements Filter {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(DelegateFilter.class);

  private ServletContext m_servletContext;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_servletContext = filterConfig.getServletContext();
  }

  @Override
  public void doFilter(final ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {

    new ServletFilterDelegate().delegateServiceMethod(request, response, new ServletFilterDelegate.IServiceCallback() {
      @Override
      public void service(ServletRequest reqInner, ServletResponse resInner) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) reqInner;
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String remoteAddr = httpServletRequest.getRemoteAddr();
        try {
          chain.doFilter(reqInner, resInner);
        }
        catch (ServletException e) {
          LOG.error("ServletException\n UserAgent: {0}\nRemoteIP: {1}", userAgent, remoteAddr);
          throw e;
        }
        catch (IOException e) {
          LOG.error("IOException\n UserAgent: {0}\nRemoteIP: {1}", userAgent, remoteAddr);
          throw e;
        }
        catch (IllegalStateException e) {
          LOG.error("IllegalStateException\n UserAgent: {0}\nRemoteIP: {1}", userAgent, remoteAddr);
          httpServletRequest.getSession().invalidate();
          throw e;
        }
      }

      @Override
      public ServletContext getServletContext() {
        return m_servletContext;
      }
    });
  }

  @Override
  public void destroy() {
  }

}
