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
import org.eclipse.scout.rt.server.commons.authentication.ConfigFileCredentialVerifier;
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAuthenticator;
import org.eclipse.scout.rt.server.commons.authentication.FormAuthenticator;
import org.eclipse.scout.rt.server.commons.authentication.FormAuthenticator.FormAuthConfig;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAuthenticator;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAuthenticator.TrivialAuthConfig;

/**
 * <h3>{@link UiHtmlServletFilter}</h3>
 * This is the main servlet filter used for the html ui.
 *
 * @author ${userName}
 */
public class UiHtmlServletFilter implements Filter {

  private TrivialAuthenticator m_trivialAuthenticator;
  private DevelopmentAuthenticator m_devAuthenticator;
  private FormAuthenticator m_formAuthenticator;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_trivialAuthenticator = BEANS.get(TrivialAuthenticator.class);
    m_trivialAuthenticator.init(new TrivialAuthConfig().withExclusionFilter(filterConfig.getInitParameter("filter-exclude")));

    m_devAuthenticator = BEANS.get(DevelopmentAuthenticator.class);
    m_devAuthenticator.init();

    m_formAuthenticator = BEANS.get(FormAuthenticator.class);
    m_formAuthenticator.init(new FormAuthConfig().withCredentialVerifier(BEANS.get(ConfigFileCredentialVerifier.class)));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    if (m_formAuthenticator.handle(req, resp, chain)) {
      return;
    }

    if (m_trivialAuthenticator.handle(req, resp, chain)) {
      return;
    }

    if (m_devAuthenticator.handle(req, resp, chain)) {
      return;
    }

    m_formAuthenticator.forwardToLoginForm(req, resp);
  }

  @Override
  public void destroy() {
    m_formAuthenticator.destroy();
    m_formAuthenticator = null;

    m_devAuthenticator.destroy();
    m_devAuthenticator = null;

    m_trivialAuthenticator.destroy();
    m_trivialAuthenticator = null;
  }
}
