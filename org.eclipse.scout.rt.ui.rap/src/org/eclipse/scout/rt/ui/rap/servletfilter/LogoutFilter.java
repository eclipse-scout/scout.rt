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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;

/**
 * Filter which checks for a query parameter called "doLogout" and invalidates the session if the parameter is set.
 * <p>
 * After invalidating the session a redirect is sent and the filter chain gets interrupted (no more filters will be
 * executed). The location to redirect to may be given by request parameter or filter property, whereas the filter
 * property has higher priority.<br>
 * Example:<br>
 * <code>org.eclipse.scout.rt.ui.rap.servletfilter.LogoutFilter#redirectUrl=res/logout.html</code>
 * <p>
 * 
 * @since 3.8.2
 */
public class LogoutFilter implements Filter {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogoutFilter.class);
  private static final String DOIT_PARAM = "doit";
  public final static String LOGOUT_PARAM = "doLogout";
  public final static String REDIRECT_PARAM = "redirectUrl";
  public final static String RELOAD_ON_SESSION_TIMEOUT_PARAM = "reloadOnSessionTimeout";

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

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpSession session = httpRequest.getSession(false);

    if (session == null || session.isNew()) {
      boolean abortChain = handleSessionTimeout(config, httpRequest, httpResponse);
      if (abortChain) {
        return;
      }
    }

    String logoutParam = request.getParameter(LOGOUT_PARAM);
    if (logoutParam == null) {
      chain.doFilter(request, response);
      return;
    }

    String doitParamValue = request.getParameter(DOIT_PARAM);
    if (!StringUtility.hasText(doitParamValue)) {
      forceClientSideReload(httpRequest, httpResponse);
      return;
    }

    if (session != null) {
      session.invalidate();
      LOG.info("Logout successful.");
    }

    String redirectLocation = computeRedirectLocation(config, httpRequest);
    if (StringUtility.hasText(redirectLocation)) {
      httpResponse.sendRedirect(httpResponse.encodeRedirectURL(redirectLocation));
    }
  }

  /**
   * Detects whether the current request is an ajax call. If true and the session is invalid or expired a response will
   * be sent back which triggers a reload of the page.
   * <p>
   * This makes sure the rap client won't break if a login site is returned instead of a valid json response in case of
   * a session timeout (happens for example if form authentication on tomcat is used).
   * <p>
   * This detection is only active if the filter property {@value} #RELOAD_ON_SESSION_TIMEOUT_PARAM} is set to true.
   */
  protected boolean handleSessionTimeout(FilterConfigInjection.FilterConfig config, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String reloadOnSessionTimeout = config.getInitParameter(RELOAD_ON_SESSION_TIMEOUT_PARAM);
    if (!Boolean.parseBoolean(reloadOnSessionTimeout)) {
      return false;
    }

    String contentType = request.getHeader("Content-Type");
    if (contentType == null || !contentType.contains("application/json")) {
      return false;
    }

    //Clean url, otherwise IE might run into an infinite loop
    String referer = request.getHeader("Referer");
    int index = referer.indexOf("#");
    if (index >= 0) {
      referer = referer.substring(0, index);
    }
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{ \"head\": { \"redirect\": \"" + referer + "\"},\"operations\": [] }");

    LOG.info("Site reload after session timeout initiated.");
    return true;
  }

  /**
   * Returns a html site to the client which just reloads the page with the additional parameter doit=true. This
   * roundtrip is important to make sure the actual rap site is left before doing the session invalidation. Otherwise
   * the ajax calls could interfere with the form authentication of tomcat. The result would be a redirect by tomcat to
   * such an ajax call instead of the actual site.
   */
  protected void forceClientSideReload(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String redirectHtmlTemplate = ""
        + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
        + "<html>"
        + "<head>"
        + "  <script language=\"javascript\">"
        + "  function redirect() {location.replace('#redirectUrl#');}"
        + "  </script>"
        + "<title></title>"
        + "</head>"
        + "<body onLoad=\"redirect()\"></body>"
        + "</html>";

    String servletPath = request.getServletPath();
    if (servletPath.length() > 0 && '/' == servletPath.charAt(0)) {
      servletPath = servletPath.substring(1);
    }
    servletPath += "?" + LOGOUT_PARAM + "=true";
    servletPath += "&" + DOIT_PARAM + "=true";
    String redirectParamValue = request.getParameter(REDIRECT_PARAM);
    if (StringUtility.hasText(redirectParamValue)) {
      servletPath += "&" + REDIRECT_PARAM + "=" + redirectParamValue;
    }
    servletPath = response.encodeRedirectURL(servletPath);
    String redirectHtml = redirectHtmlTemplate.replace("#redirectUrl#", servletPath);
    response.setContentType("text/html");
    response.getWriter().print(redirectHtml);
  }

  protected String computeRedirectLocation(FilterConfigInjection.FilterConfig config, HttpServletRequest request) {
    String redirectLocation = config.getInitParameter(REDIRECT_PARAM);
    if (!StringUtility.hasText(redirectLocation)) {
      redirectLocation = request.getParameter(REDIRECT_PARAM);
    }
    if (!StringUtility.hasText(redirectLocation)) {
      //If neither the filter property nor the request parameter is set the actual servlet is used as redirect location
      redirectLocation = request.getServletPath();
      if (redirectLocation.length() > 0 && '/' == redirectLocation.charAt(0)) {
        redirectLocation = redirectLocation.substring(1);
      }
    }

    return redirectLocation;
  }
}
