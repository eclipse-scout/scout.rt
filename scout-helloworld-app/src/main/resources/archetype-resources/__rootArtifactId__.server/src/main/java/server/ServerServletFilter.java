#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.server;

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
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAccessController;
import org.eclipse.scout.rt.server.commons.authentication.ServiceTunnelAccessTokenAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController.TrivialAuthConfig;

/**
 * This is the main server side servlet filter.
 *
 * @author ${userName}
 */
public class ServerServletFilter implements Filter {

  private TrivialAccessController m_trivialAccessController;
  private ServiceTunnelAccessTokenAccessController m_tunnelAccessController;
  private DevelopmentAccessController m_developmentAccessController;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_trivialAccessController = BEANS.get(TrivialAccessController.class).init(new TrivialAuthConfig().withExclusionFilter(filterConfig.getInitParameter("filter-exclude")));
    m_tunnelAccessController = BEANS.get(ServiceTunnelAccessTokenAccessController.class).init();
    m_developmentAccessController = BEANS.get(DevelopmentAccessController.class).init();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    if (m_trivialAccessController.handle(req, resp, chain)) {
      return;
    }

    if (m_tunnelAccessController.handle(req, resp, chain)) {
      return;
    }

    if (m_developmentAccessController.handle(req, resp, chain)) {
      return;
    }

    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  @Override
  public void destroy() {
    m_developmentAccessController.destroy();
    m_tunnelAccessController.destroy();
    m_trivialAccessController.destroy();
  }
}
