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
package org.eclipse.scout.rt.ui.rap.servletfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;

/**
 * Filter which checks for a query parameter called "doLogout" and invalidates the session if the parameter is set.
 * <p>
 * After invalidating the session a redirect to the originally requested site is sent and the filter chain gets
 * interrupted (no more filters will be executed).<br>
 * Alternatively the "redirectUrl" parameter can be specified in the config.ini. If this parameter is set, the redirect
 * will follow the given url instead of the originally requested site.<br>
 * Example:<br>
 * <code>org.eclipse.scout.rt.ui.rap.servletfilter.LogoutFilter#redirectUrl=res/logout.html</code>
 * <p>
 * 
 * @since 3.8.2
 */
public class LogoutFilter implements Filter {

  public final static String REDIR_INIT_PARAM = "redirectUrl";
  public final static String LOGOUT_PARAM = "doLogout";

  private FilterConfigInjection m_injection;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    m_injection = new FilterConfigInjection(config0, getClass());
  }

  @Override
  public void destroy() {
    m_injection = null;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(request);
    if (!config.isActive()) {
      chain.doFilter(request, response);
      return;
    }

    String logoutParam = request.getParameter(LOGOUT_PARAM);
    if (logoutParam == null) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      chain.doFilter(request, response);
      return;
    }

    session.invalidate();

    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String redirPath = config.getInitParameter(REDIR_INIT_PARAM);
    if (!StringUtility.hasText(redirPath)) {
      // default when no redirect URL is given
      redirPath = getRedirectUrl(httpRequest);
    }
    httpResponse.sendRedirect(httpResponse.encodeRedirectURL(redirPath));
  }

  protected String getRedirectUrl(HttpServletRequest httpRequest) {
    String servletPath = httpRequest.getServletPath();
    if (servletPath.length() > 0 && '/' == servletPath.charAt(0)) {
      servletPath = servletPath.substring(1);
    }

    return servletPath;
  }
}
