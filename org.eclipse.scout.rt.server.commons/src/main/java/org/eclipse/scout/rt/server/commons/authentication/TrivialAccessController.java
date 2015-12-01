/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Access controller to fast-check user's access to requested resources, and handles '/login' and '/logout' requests.
 * Requests to '/auth' are not handled.
 * <p>
 * The name trivial results from this controller's characteristics to only evaluate pre-calculated authentication
 * information, but never performs an expensive verification. That is why this controller is to be installed as the very
 * first access controller.
 * <p>
 * For requests to '/login', the request is dispatched to login.html<br/>
 * For requests to '/logout', the associated HTTP session is invalidated (if any), and the request dispatched to
 * logout.html.
 * <p>
 * This access controller continues chain if one of the following criteria applies:
 * <ul>
 * <li>if running within a {@link Subject} that contains the principal as set in
 * {@link HttpServletRequest#getRemoteUser()};</li>
 * <li>if {@link HttpServletRequest#getRemoteUser()} or {@link HttpServletRequest#getUserPrincipal()} is set;</li>
 * <li>if a {@link HttpSession} exists with a {@link Principal} set in
 * {@link ServletFilterHelper#SESSION_ATTRIBUTE_FOR_PRINCIPAL};</li>
 * <li>if the request path is subject for exclusion;
 * </ul>
 * Also, any request to "" is redirected to "/".
 *
 * @since 5.1
 */
public class TrivialAccessController implements IAccessController {

  private TrivialAuthConfig m_config;

  public TrivialAccessController init(final TrivialAuthConfig config) throws ServletException {
    m_config = config;
    return this;
  }

  @Override
  public boolean handle(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    switch (getTarget(request)) {
      case "/login":
        handleLoginRequest(request, response);
        return true;
      case "/logout":
        handleLogoutRequest(request, response);
        return true;
      case "/auth":
        return false;
      default:
        return handleRequest(request, response, chain);
    }
  }

  @Override
  public void destroy() {
    // NOOP
  }

  protected boolean handleRequest(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    // Is running within a valid subject?
    if (BEANS.get(ServletFilterHelper.class).isRunningWithValidSubject(request)) {
      chain.doFilter(request, response);
      return true;
    }

    // Is already authenticated?
    final Principal principal = BEANS.get(ServletFilterHelper.class).findPrincipal(request, m_config.getPrincipalProducer());
    if (principal != null) {
      BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, request, response, chain);
      return true;
    }

    // Is request path excluded from authentication?
    if (m_config.getPathInfoFilter().accepts(StringUtility.emptyIfNull(request.getServletPath()) + StringUtility.emptyIfNull(request.getPathInfo()))) {
      chain.doFilter(request, response);
      return true;
    }

    // Is a request to base URL? (copy from UiServlet.doGet)
    final String contextPath = request.getServletContext().getContextPath();
    if (StringUtility.hasText(contextPath) && request.getRequestURI().endsWith(contextPath)) {
      response.sendRedirect(request.getRequestURI() + "/");
      return true;
    }

    return false;
  }

  /**
   * Method invoked on a request targeted to '/login'.<br/>
   * If login page is installed, the default implementation dispatches to '/login.html' page so that the user can enter
   * username and password.
   */
  protected void handleLoginRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
    if (m_config.isLoginPageInstalled()) {
      BEANS.get(ServletFilterHelper.class).forwardToLoginForm(request, response);
    }
  }

  /**
   * Method invoked on a request targeted to '/logout'.<br/>
   * The default implementation invalidates HTTP session (if any) and if logout page is installed, dispatches the
   * request to '/logout.html' page.
   */
  protected void handleLogoutRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
    BEANS.get(ServletFilterHelper.class).doLogout(request);
    if (m_config.isLoginPageInstalled()) {
      BEANS.get(ServletFilterHelper.class).forwardToLogoutForm(request, response);
    }
  }

  protected String getTarget(final HttpServletRequest request) {
    final String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      return pathInfo;
    }

    final String requestURI = request.getRequestURI();
    return requestURI.substring(requestURI.lastIndexOf('/'));
  }

  /**
   * Configuration for {@link TrivialAccessController}.
   */
  public static class TrivialAuthConfig {

    private boolean m_enabled = true;
    private IPrincipalProducer m_principalProducer = BEANS.get(RemoteUserPrincipalProducer.class);
    private PathInfoFilter m_exclusionFilter;
    private boolean m_loginPageInstalled = false;

    public boolean isEnabled() {
      return m_enabled;
    }

    public TrivialAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    public TrivialAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }

    public PathInfoFilter getPathInfoFilter() {
      return m_exclusionFilter;
    }

    /**
     * Exclude resources from authentication.
     * <p>
     * Filter format: separate resources by comma, newline or whitespace; usage of wildcard (*) character is supported;
     */
    public TrivialAuthConfig withExclusionFilter(final String exclusionFilter) {
      m_exclusionFilter = new PathInfoFilter(exclusionFilter);
      return this;
    }

    public boolean isLoginPageInstalled() {
      return m_loginPageInstalled;
    }

    /**
     * Indicates whether this web application has a login and logout page installed, meaning that the request is
     * dispatched to that page when requesting to log in, or upon logged out.
     */
    public TrivialAuthConfig withLoginPageInstalled(final boolean loginPageInstalled) {
      m_loginPageInstalled = loginPageInstalled;
      return this;
    }
  }
}
