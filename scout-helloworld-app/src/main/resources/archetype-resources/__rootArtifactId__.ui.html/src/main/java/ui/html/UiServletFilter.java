#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.ui.html;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier;
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController.FormBasedAuthConfig;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController.TrivialAuthConfig;

/**
 * This is the main servlet filter for the HTML UI.
 *
 * @author ${userName}
 */
public class UiServletFilter implements Filter {

  private TrivialAccessController m_trivialAccessController;
  private FormBasedAccessController m_formBasedAccessController;
  private DevelopmentAccessController m_developmentAccessController;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_trivialAccessController = BEANS.get(TrivialAccessController.class)
        .init(new TrivialAuthConfig()
            .withExclusionFilter(filterConfig.getInitParameter("filter-exclude"))
            .withLoginPageInstalled(true));
    m_formBasedAccessController = BEANS.get(FormBasedAccessController.class)
        .init(new FormBasedAuthConfig()
            .withCredentialVerifier(BEANS.get(ConfigFileCredentialVerifier.class)));
    m_developmentAccessController = BEANS.get(DevelopmentAccessController.class).init();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    if (m_trivialAccessController.handle(req, resp, chain)) {
      return;
    }

    if (m_formBasedAccessController.handle(req, resp, chain)) {
      return;
    }

    if (m_developmentAccessController.handle(req, resp, chain)) {
      return;
    }

    BEANS.get(ServletFilterHelper.class).forwardToLoginForm(req, resp);
  }

  @Override
  public void destroy() {
    m_developmentAccessController.destroy();
    m_formBasedAccessController.destroy();
    m_trivialAccessController.destroy();
  }
}
