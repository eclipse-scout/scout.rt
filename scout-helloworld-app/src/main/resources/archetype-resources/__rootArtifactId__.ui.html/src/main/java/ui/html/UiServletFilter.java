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
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController.FormBasedAuthConfig;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController.TrivialAuthConfig;

/**
 * <h3>{@link UiServletFilter}</h3>
 * This is the main servlet filter used for the HTML UI.
 *
 * @author ${userName}
 */
public class UiServletFilter implements Filter {

  private FormBasedAccessController m_formBasedAccessController;
  private TrivialAccessController m_trivialAccessController;
  private DevelopmentAccessController m_developmentAccessController;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_formBasedAccessController = BEANS.get(FormBasedAccessController.class)
        .init(new FormBasedAuthConfig()
            .withCredentialVerifier(BEANS.get(ConfigFileCredentialVerifier.class)));
    m_trivialAccessController = BEANS.get(TrivialAccessController.class)
        .init(new TrivialAuthConfig()
            .withExclusionFilter(filterConfig.getInitParameter("filter-exclude")));
    m_developmentAccessController = BEANS.get(DevelopmentAccessController.class).init();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    if (m_formBasedAccessController.handle(req, resp, chain)) {
      return;
    }

    if (m_trivialAccessController.handle(req, resp, chain)) {
      return;
    }

    if (m_developmentAccessController.handle(req, resp, chain)) {
      return;
    }

    m_formBasedAccessController.forwardToLoginForm(req, resp);
  }

  @Override
  public void destroy() {
    m_developmentAccessController.destroy();
    m_trivialAccessController.destroy();
    m_formBasedAccessController.destroy();
  }
}
