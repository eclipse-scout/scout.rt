#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.ui.html;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.servlet.filter.authentication.DevelopmentAuthenticator;
import org.eclipse.scout.rt.server.commons.servlet.filter.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.commons.servlet.filter.authentication.TrivialAuthenticator;

/**
 * <h3>{@link UiHtmlServletFilter}</h3>
 *
 * @author ${userName}
 */
public class UiHtmlServletFilter implements Filter {

  private ServletFilterHelper m_servletFilterHelper;
  private TrivialAuthenticator m_trivialAuthenticator;
  private DevelopmentAuthenticator m_devAuthenticator;
  private FormAuthenticator m_formAuthenticator;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_trivialAuthenticator = BEANS.get(TrivialAuthenticator.class);
    m_servletFilterHelper = BEANS.get(ServletFilterHelper.class);
    m_devAuthenticator = BEANS.get(DevelopmentAuthenticator.class);
    m_formAuthenticator = BEANS.get(FormAuthenticator.class);

    m_trivialAuthenticator.init(filterConfig);
    m_devAuthenticator.init(filterConfig);
    m_formAuthenticator.init(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    //call to /auth is handled in advance by the corresponding form auth handler
    if (isLoginFormAction(req)) {
      if (!m_formAuthenticator.handle(req, resp)) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
      return;
    }

    if (isLogoutRequest(req)) {
      m_servletFilterHelper.doLogout(req);
      m_servletFilterHelper.forwardToLogoutForm(req, resp);
      return;
    }

    if (isLoginRequest(req)) {
      m_servletFilterHelper.forwardToLoginForm(req, resp);
      return;
    }

    if (m_trivialAuthenticator.handle(req, resp, chain)) {
      return;
    }

    if (m_devAuthenticator.handle(req, resp, chain)) {
      return;
    }

    m_servletFilterHelper.forwardToLoginForm(req, resp);
  }

  @Override
  public void destroy() {
    m_trivialAuthenticator.destroy();
    m_devAuthenticator.destroy();
    m_formAuthenticator.destroy();
    m_trivialAuthenticator = null;
    m_devAuthenticator = null;
    m_formAuthenticator = null;
    m_servletFilterHelper = null;
  }

  protected boolean isLogoutRequest(HttpServletRequest req) {
    return "/logout".equals(req.getPathInfo());
  }

  protected boolean isLoginRequest(HttpServletRequest req) {
    return "/login".equals(req.getPathInfo());
  }

  protected boolean isLoginFormAction(HttpServletRequest req) {
    return "/auth".equals(req.getPathInfo());
  }
}
